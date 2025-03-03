package com.example.wayhome.service.impl;

import com.example.wayhome.graphEntity.Edge;
import com.example.wayhome.graphEntity.Graph;
import com.example.wayhome.graphEntity.Node;
import com.example.wayhome.dto.StationTinyDTO;
import com.example.wayhome.entity.Route;
import com.example.wayhome.entity.Schedule;
import com.example.wayhome.entity.Station;
import com.example.wayhome.mapper.RouteMapper;
import com.example.wayhome.mapper.RoutePointMapper;
import com.example.wayhome.mapper.ScheduleMapper;
import com.example.wayhome.mapper.StationMapper;
import com.example.wayhome.service.PathCalculateService;
import com.example.wayhome.vo.PointTransferVO;
import com.example.wayhome.vo.PointVO;
import com.example.wayhome.vo.SolutionVO;
import com.example.wayhome.vo.StationInRouteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.*;
import java.util.concurrent.TimeUnit;


@Service
public class PathCalculateServiceImpl implements PathCalculateService {

    @Autowired
    private ScheduleMapper scheduleMapper;

    @Autowired
    private RoutePointMapper routePointMapper;

    @Autowired
    private StationMapper stationMapper;

    @Autowired
    private RouteMapper routeMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Value("${redis.expiration}")
    private Long expireTime;

    @Value("${speed}")
    private Double speed;


    public record MultiMapHolder(
            Map<Long, List<Integer>> routeDirectionSave,
            Map<Long, List<StationTinyDTO>> routePointSave,
            Map<String, Long> stationIDToUniqueTag,
            Map<Long, String> uniqueTagToStationID,
            Map<Long, List<Long>> stationBelongedRoutes,
            Map<Long, Long> uniqueTagToPointID,
            Long curStationID
    ) {}

    @Transactional
    public MultiMapHolder prepareGraphBuild(Long startStaID, Long endStaID, LocalDateTime searchTime, Integer cityID) {
        Map<Long, List<Integer>> routeDirectionSave = getRouteByTime(searchTime, cityID);
        List<Long> routeIDList = new ArrayList<>();
        routeDirectionSave.forEach((routeID, direction) -> routeIDList.add(routeID));
        List<PointVO> pointVOS = routePointMapper.pointInRouteQuery(routeIDList);

        // 路径分割
        Map<Long, List<StationTinyDTO>> routePointSave = new HashMap<>();
        boolean hasRoutePassByStartStation = false;
        boolean hasRoutePassByEndStation = false;
        // 用"站点:路线"表明一个唯一点, 为方便处理存储点到唯一标识符的双向映射关系
        Map<String, Long> stationIDToUniqueTag = new HashMap<>();
        Map<Long, String> uniqueTagToStationID = new HashMap<>();
        Map<Long, List<Long>> stationBelongedRoutes = new HashMap<>();
        Map<Long, Long> uniqueTagToPointID = new HashMap<>();
        Long curStationID = 0L;
        for(PointVO pointVO: pointVOS) {
            Long routeID = pointVO.getRouteID();
            Long staID = pointVO.getStaID();
            Long pointID = pointVO.getPointID();

            if(!routePointSave.containsKey(pointVO.getRouteID())) {
                routePointSave.put(routeID, new ArrayList<>());
            }
            if(startStaID.equals(staID)) hasRoutePassByStartStation = true;
            if(endStaID.equals(staID)) hasRoutePassByEndStation = true;
            StationTinyDTO stationTinyDTO = new StationTinyDTO();
            stationTinyDTO.setStaID(staID == null ? -1L : staID);
            stationTinyDTO.setPointID(pointID);

            // 将控制点/站点的地理位置信息交给 redis 进行保存
            redisTemplate.opsForGeo().add("places", new Point(pointVO.getPointLng(), pointVO.getPointLat()), pointID);
            redisTemplate.expire("places", expireTime, TimeUnit.SECONDS);
            routePointSave.get(routeID).add(stationTinyDTO);

            // 存储站点映射关系
            if(staID != null) {
                String detailTag = staID + ":" + routeID;
                stationIDToUniqueTag.put(detailTag, curStationID);
                uniqueTagToStationID.put(curStationID, detailTag);
                curStationID++;
                if(!stationBelongedRoutes.containsKey(staID))
                    stationBelongedRoutes.put(staID, new ArrayList<>());
                stationBelongedRoutes.get(staID).add(routeID);
                uniqueTagToPointID.put(curStationID, pointID);
            }
        }
        // 当前运行期间,没有任何一条路线经过相关站点
        if(!hasRoutePassByStartStation || !hasRoutePassByEndStation) return null;
        return new MultiMapHolder(routeDirectionSave, routePointSave, stationIDToUniqueTag,
                                        uniqueTagToStationID, stationBelongedRoutes, uniqueTagToPointID, curStationID);
    }


    public <T> List<SolutionVO<T>> getSolutions(T finalAns, List<List<Long>> ansPath,
                                            Map<Long, String> uniqueTagToStationID, Integer cityID,
                                            Map<Long, List<StationTinyDTO>> routePointSave, Graph<T> graph) {
        List<SolutionVO<T>> solutions = new ArrayList<>();
        ansPath.forEach(path -> {
            SolutionVO<T> solution = new SolutionVO<>();
            solution.setDistance(finalAns);
            List<StationInRouteVO> routeTmp = new ArrayList<>();
            path.forEach(stationUniqueTag -> {
                String[] stationAndRoute = uniqueTagToStationID.get(stationUniqueTag).split(":");
                Long staID = Long.valueOf(stationAndRoute[0]);
                Long routeID = Long.valueOf(stationAndRoute[1]);
                StationInRouteVO stationInRouteVO = new StationInRouteVO();
                stationInRouteVO.setStaID(staID);
                stationInRouteVO.setRouteID(routeID);
                String stationKey = "stationMap:staID:" + staID;
                String staName = null;
                if(redisTemplate.hasKey(stationKey)) {
                    staName = (String)redisTemplate.opsForValue().get(stationKey);
                    staName = staName.split(":")[0];
                } else {
                    Station station = stationMapper.selectById(staID);
                    staName = station.getStaName();
                    redisTemplate.opsForValue().set(stationKey, staName + ":" + cityID);
                    redisTemplate.expire(stationKey, expireTime, TimeUnit.SECONDS);
                }
                stationInRouteVO.setStaName(staName);

                String routeKey = "routeMap:routeID:" + routeID;
                String routeName = null;
                if(redisTemplate.hasKey(routeKey)) {
                    routeName = (String) redisTemplate.opsForValue().get(routeKey);
                    routeName = routeName.split(":")[0];
                } else {
                    Route route = routeMapper.selectById(routeID);
                    routeName = route.getRouteName();
                    redisTemplate.opsForValue().set(routeKey, routeName + ":" + routeID);
                    redisTemplate.expire(routeKey, expireTime, TimeUnit.SECONDS);
                }
                stationInRouteVO.setRouteName(routeName);

                if(routeTmp.isEmpty() || Objects.equals(routeTmp.get(routeTmp.size() - 1).getRouteID(), routeID)) {
                    routeTmp.add(stationInRouteVO);
                } else {
                    solution.getScheme().add(new ArrayList<>(routeTmp));
                    StationInRouteVO lastSta = routeTmp.get(routeTmp.size() - 1);
                    StationInRouteVO newSta = new StationInRouteVO();
                    newSta.setStaID(lastSta.getStaID());
                    newSta.setStaName(lastSta.getStaName());
                    newSta.setRouteID(routeID);
                    newSta.setRouteName(routeName);
                    routeTmp.clear();
                    routeTmp.add(newSta);
                    StationInRouteVO curSta = new StationInRouteVO();
                    curSta.setStaID(staID);
                    curSta.setStaName(staName);
                    curSta.setRouteID(routeID);
                    curSta.setRouteName(routeName);
                    routeTmp.add(curSta);
                }
            });
            solution.getScheme().add(routeTmp);
            solution.getScheme().forEach(scheme -> {
                // 搜寻某条路线上的框定站点内的所有控制点信息
                List<StationTinyDTO> stationTinyDTOS = routePointSave.get(scheme.get(0).getRouteID());
                List<PointTransferVO> allPointByRouteInStationLimit = getAllPointByRouteInStationLimit(scheme, stationTinyDTOS, graph);
                solution.getRouteDetail().add(allPointByRouteInStationLimit);
            });
            solution.setRealDistance(graph.getRealDistance());
            solutions.add(solution);
        });
        return solutions;
    }

    // 获取当前时间段还在运行的路线和方向信息
    public Map<Long, List<Integer>> getRouteByTime(LocalDateTime searchTime, Integer cityID) {
        Map<String, String> workdays = Map.of(
                "MONDAY", "周一",
                "TUESDAY", "周二",
                "WEDNESDAY", "周三",
                "THURSDAY", "周四",
                "FRIDAY", "周五",
                "SATURDAY", "周六",
                "SUNDAY", "周日"
        );
        String _workday = workdays.get(searchTime.getDayOfWeek().toString());
        LocalTime localTime = searchTime.toLocalTime();
        // 将 LocalTime 转换为 LocalDateTime（假设是某天的时间）
        LocalDateTime utcDateTime = LocalDateTime.of(LocalDate.now(), localTime);
        // 将 LocalDateTime 转换为 UTC 时区的 ZonedDateTime
        ZonedDateTime utcZoned = utcDateTime.atZone(ZoneOffset.UTC);
        // 转换到东八区（Asia/Shanghai）
        ZoneId shanghaiZone = ZoneId.of("Asia/Shanghai");
        ZonedDateTime shanghaiZoned = utcZoned.withZoneSameInstant(shanghaiZone);
        // 提取东八区时间（转换为 LocalTime）
        LocalTime _time = shanghaiZoned.toLocalTime();
        List<Schedule> schedules = scheduleMapper.scheduleSelectByTime(_workday, _time, cityID);

        Map<Long, List<Integer>> routeDirectionSave = new HashMap<>();
        // 一条线路,正反两个方向, 存在三种可能: 只有正向方向、只有反向方向、正反方向均存在
        // 正方向为记1, 负方向记为-1,均存在记为0
        schedules.forEach(schedule -> {
            Long routeID = schedule.getRouteID();
            int direction = schedule.getDirection() ? 0 : 1;
            Integer deInterval = schedule.getDeInterval();
            if(!routeDirectionSave.containsKey(routeID)) { // 第一个位置上存储
                routeDirectionSave.put(routeID, new ArrayList<>(List.of(-1, -1)));
            }
            routeDirectionSave.get(routeID).set(direction, deInterval);
        });
        return routeDirectionSave;
    }

    private double heuristic(Long pointAID, Long pointBID, Map<Long, Long> coordinates) {
        Long pointA = coordinates.get(pointAID);
        Long pointB = coordinates.get(pointBID);
        Distance distance = redisTemplate.opsForGeo().distance("places", pointA, pointB, RedisGeoCommands.DistanceUnit.METERS);
        return (distance != null) ? distance.getValue() : Double.MAX_VALUE;
    }

    private List<Long> reconstructPath(Map<Long, Long> cameFrom, long currentID) {
        List<Long> path = new ArrayList<>();
        while (cameFrom.containsKey(currentID)) {
            path.add(currentID);
            currentID = cameFrom.get(currentID);
        }
        path.add(currentID);
        Collections.reverse(path);
        return path;
    }

    public List<Long> aStarSearch(long startID, long goalID, Map<Long, Long> coordinates, Graph<Double> graph) {
        Map<Long, List<Edge<Double>>> G = graph.getG();

        PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(Node::getF));
        Map<Long, Double> gScore = new HashMap<>(); // 起点到当前点的真实代价
        Map<Long, Double> fScore = new HashMap<>(); // 估计的总代价
        Map<Long, Long> cameFrom = new HashMap<>(); // 路径回溯


        gScore.put(startID, 0.0);
        fScore.put(startID, heuristic(startID, goalID, coordinates) / speed);
        openSet.add(new Node(startID, 0.0, fScore.get(startID)));

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            long currentID = current.getId();

            if (currentID == goalID) {
                graph.setRecordDistance(gScore.get(goalID));
                return reconstructPath(cameFrom, goalID);
            }

            if (!G.containsKey(currentID)) continue;
            for (Edge<Double> edge : G.get(currentID)) {
                long neighborID = edge.getNextID();
                double tentative_gScore = gScore.getOrDefault(currentID, Double.MAX_VALUE) + edge.getValue();

                if (tentative_gScore < gScore.getOrDefault(neighborID, Double.MAX_VALUE)) {
                    cameFrom.put(neighborID, currentID);
                    gScore.put(neighborID, tentative_gScore);
                    double fScoreValue = tentative_gScore + heuristic(neighborID, goalID, coordinates) / speed;
                    fScore.put(neighborID, fScoreValue);
                    openSet.add(new Node(neighborID, tentative_gScore, fScoreValue));
                }
            }
        }
        return Collections.emptyList();
    }

    public List<List<Long>> BFSSearch(long startID, long goalID, Graph<Integer> graph) {
        Map<Long, List<Edge<Integer>>> g = graph.getG();

        Queue<List<Long>> queue = new LinkedList<>();
        queue.offer(new ArrayList<>(List.of(startID)));

        Map<Long, Integer> minTransfers = new HashMap<>();
        minTransfers.put(startID, 0);

        List<List<Long>> result = new ArrayList<>();
        int minTransferCount = Integer.MAX_VALUE;

        while (!queue.isEmpty()) {
            List<Long> path = queue.poll();
            Long lastNode = path.get(path.size() - 1);
            int currentTransfers = minTransfers.get(lastNode);

            // 如果当前路径已经超过最优换乘次数，则直接跳过
            if (currentTransfers > minTransferCount) continue;

            // 到达终点，更新结果
            if (lastNode.equals(goalID)) {
                if (currentTransfers < minTransferCount) {
                    minTransferCount = currentTransfers;
                    graph.setRecordDistance(minTransferCount);
                    result.clear(); // 之前的方案就无效了，清空
                }
                result.add(new ArrayList<>(path));
                continue;
            }

            // 遍历邻接点
            for (Edge<Integer> edge : g.getOrDefault(lastNode, new ArrayList<>())) {
                Long nextNode = edge.getNextID();
                int nextTransfers = currentTransfers + edge.getValue(); // 计算换乘次数

                // 只允许换乘次数最少的路径进入队列
                if (!minTransfers.containsKey(nextNode) || nextTransfers < minTransfers.get(nextNode)) {
                    minTransfers.put(nextNode, nextTransfers);
                    List<Long> newPath = new ArrayList<>(path);
                    newPath.add(nextNode);
                    queue.offer(newPath);
                }
            }
        }
        return result;
    }

    // 获取具体路径
    public List<PointTransferVO> getAllPointByRouteInStationLimit(List<StationInRouteVO> stations, List<StationTinyDTO> route, Graph<?> graph) {
        Long startStaID = stations.get(0).getStaID();
        Long endStaID = stations.get(stations.size() - 1).getStaID();
        boolean tag = false;
        List<PointTransferVO> detailPath = new ArrayList<>();
        for(int i = 0; i < route.size(); i++) {
            if(Objects.equals(route.get(i).getStaID(), startStaID)) {
                tag = true;
            }
            if(tag) {
                List<Point> places = redisTemplate.opsForGeo().position("places", route.get(i).getPointID());
                PointTransferVO point = new PointTransferVO();
                point.setPoint(places.get(0));
                point.setStaID(route.get(i).getStaID());
                detailPath.add(point);
                if(detailPath.size() > 1) {
                    Long lastPointID = route.get(i - 1).getPointID();
                    Long curPointID = route.get(i).getPointID();
                    Distance distance = redisTemplate.opsForGeo().distance("places", lastPointID, curPointID, RedisGeoCommands.DistanceUnit.METERS);
                    if(graph.getRealDistance() == null) graph.setRealDistance(0.0);
                    graph.setRealDistance(graph.getRealDistance() + distance.getValue());
                }
            }
            if(Objects.equals(route.get(i).getStaID(), endStaID)) break;
        }

        tag = false;
        for(int i = route.size() - 1; i >= 0; i--) {
            if(Objects.equals(route.get(i).getStaID(), startStaID)) {
                tag = true;
            }
            if(tag) {
                List<Point> places = redisTemplate.opsForGeo().position("places", route.get(i).getPointID());
                PointTransferVO point = new PointTransferVO();
                point.setPoint(places.get(0));
                point.setStaID(route.get(i).getStaID());
                detailPath.add(point);
                if(detailPath.size() > 1) {
                    Long lastPointID = route.get(i + 1).getPointID();
                    Long curPointID = route.get(i).getPointID();
                    Distance distance = redisTemplate.opsForGeo().distance("places", lastPointID, curPointID, RedisGeoCommands.DistanceUnit.METERS);
                    if(graph.getRealDistance() == null) graph.setRealDistance(0.0);
                    graph.setRealDistance(graph.getRealDistance() + distance.getValue());
                }
            }
            if(Objects.equals(route.get(i).getStaID(), endStaID)) break;
        }

        return detailPath;
    }

    @Override
    public List<SolutionVO<Double>> minTimeCostCal(Long startStaID, Long endStaID, LocalDateTime searchTime, Integer cityID) {
        Map<Long, List<Integer>> routeDirectionSave;
        Map<Long, List<StationTinyDTO>> routePointSave;
        Map<String, Long> stationIDToUniqueTag;
        Map<Long, String> uniqueTagToStationID;
        Map<Long, List<Long>> stationBelongedRoutes;
        Map<Long, Long> uniqueTagToPointID;
        Long curStationID;

        MultiMapHolder multiMapHolder = prepareGraphBuild(startStaID, endStaID, searchTime, cityID);
        if(multiMapHolder == null) return null;
        else {
            routeDirectionSave = multiMapHolder.routeDirectionSave;
            routePointSave = multiMapHolder.routePointSave;
            stationIDToUniqueTag = multiMapHolder.stationIDToUniqueTag;
            uniqueTagToStationID = multiMapHolder.uniqueTagToStationID;
            stationBelongedRoutes = multiMapHolder.stationBelongedRoutes;
            uniqueTagToPointID = multiMapHolder.uniqueTagToPointID;
            curStationID = multiMapHolder.curStationID;
        }

        // 建图
        long pointNum = curStationID;

        Graph<Double> graph = new Graph<>(pointNum);

        // 遍历所有路线, 单一路线内建边
        routeDirectionSave.forEach((routeID, direction) -> {
            // direction 为 -1或1 则单向建图, 否则为0双向建图
            List<StationTinyDTO> stationTinyDTOS = routePointSave.get(routeID);
            Long lastID = stationIDToUniqueTag.get(stationTinyDTOS.get(0).getStaID() + ":" + routeID);
            Long lastPointID = stationTinyDTOS.get(0).getPointID();
            Double totalDistance = 0.0;
            for(int i = 1; i < stationTinyDTOS.size(); i++) {
                Long curPointID = stationTinyDTOS.get(i).getPointID();
                Long curStaID = stationTinyDTOS.get(i).getStaID();
                Distance distance = redisTemplate.opsForGeo().distance("places", lastPointID, curPointID, RedisGeoCommands.DistanceUnit.METERS);
                totalDistance += distance.getValue();
                if(curStaID != -1) { // 当前是一个站点, 建边
                    Long curID = stationIDToUniqueTag.get(curStaID + ":" + routeID);
                    if(direction.get(0) != -1) { // 存在正向班次路径
                        graph.insertEdge(lastID, curID, totalDistance / speed);
                    }
                    if(direction.get(1) != -1) { // 存在负向班次路径
                        graph.insertEdge(curID, lastID, totalDistance / speed);
                    }
                    lastID = curID;
                    totalDistance = 0.0;
                }
            }
        });


        // 同站换乘, 建边:换乘时间/2 + 换乘站点到换成后下一个站点的时间
        stationBelongedRoutes.forEach((staID, routes) -> {
            if(routes.size() > 1) { // 存在需要于当前站点换乘的情况
                for(int i = 0; i < routes.size(); i++)
                    for(int j = 0; j < routes.size(); j++) {
                        if(i == j) continue; // 有在自己这一战下车,然后换乘到同一线路的反方向班次的情况,但是这一定会额外增加不必要的行程,和求解最短路的初衷相背
                        Long curRouteID = routes.get(i);
                        Long nextRouteID = routes.get(j);
                        Long curStation = stationIDToUniqueTag.get(staID + ":" + curRouteID);
                        Long nextStationInSameStaID = stationIDToUniqueTag.get(staID + ":" + nextRouteID);
                        // 从同路线建图中找出下一个站点集合(理论上,同一个路线只有两个方向,不考虑连续换乘最多只有两个next站点)
                        List<Edge<Double>> edges = graph.getG().get(nextStationInSameStaID);
                        edges.forEach(edge -> {
                            Long nextStation = edge.getNextID();
                            String nextStationID = uniqueTagToStationID.get(nextStation);
                            Long routeID = Long.parseLong(nextStationID.split(":")[1]);
                            if(routeID.equals(nextRouteID)) { // 不考虑连续换乘,因此直觉上感觉需要保持在一条路线上,没有详细证明过
                                // 加边
                                List<Integer> direction = routeDirectionSave.get(nextRouteID);
                                if(nextStation > nextStationInSameStaID) { // 构建uniqueTag时,是同一线路按顺序插入,因此同一线路上正向时下一个站点序号大于前一站点序号,由此判断正反方向
                                    graph.insertEdge(curStation, nextStation, edge.getValue() + direction.get(0) * 60 / 2);
                                } else { // 反向
                                    graph.insertEdge(curStation, nextStation, edge.getValue() + direction.get(1) * 60 / 2);
                                }
                            }
                        });
                    }
            }
        });

        List<Long> startStationRoutes = stationBelongedRoutes.get(startStaID);
        List<Long> endStationRoutes = stationBelongedRoutes.get(endStaID);
        double ans = Double.MAX_VALUE;
        List<Long> path = null;
        for(int i = 0; i < startStationRoutes.size(); i++)
            for(int j = 0; j < endStationRoutes.size(); j++) {
                Long startStation = stationIDToUniqueTag.get(startStaID + ":" + startStationRoutes.get(i));
                Long endStation = stationIDToUniqueTag.get(endStaID + ":" + endStationRoutes.get(j));
                List<Long> recordPath = aStarSearch(startStation, endStation, uniqueTagToPointID, graph);
                if(ans > graph.getRecordDistance()) {
                    path = new ArrayList<>(recordPath);
                    ans = graph.getRecordDistance();
                }
            }

        List<List<Long>> ansPath = new ArrayList<>();
        ansPath.add(path);

        return getSolutions(ans, ansPath, uniqueTagToStationID, cityID, routePointSave, graph);
    }


    @Override
    @Transactional
    public List<SolutionVO<Integer>> minTransfer(Long startStaID, Long endStaID, LocalDateTime searchTime, Integer cityID) {
        Map<Long, List<Integer>> routeDirectionSave;
        Map<Long, List<StationTinyDTO>> routePointSave;
        Map<String, Long> stationIDToUniqueTag;
        Map<Long, String> uniqueTagToStationID;
        Map<Long, List<Long>> stationBelongedRoutes;
        Long curStationID;

        MultiMapHolder multiMapHolder = prepareGraphBuild(startStaID, endStaID, searchTime, cityID);
        if(multiMapHolder == null) return null;
        else {
            routeDirectionSave = multiMapHolder.routeDirectionSave;
            routePointSave = multiMapHolder.routePointSave;
            stationIDToUniqueTag = multiMapHolder.stationIDToUniqueTag;
            uniqueTagToStationID = multiMapHolder.uniqueTagToStationID;
            stationBelongedRoutes = multiMapHolder.stationBelongedRoutes;
            curStationID = multiMapHolder.curStationID;
        }

        // 建图
        long pointNum = curStationID;
        Graph<Integer> graph = new Graph<>(pointNum);

        // 遍历所有路线, 单一路线内建边(单一方向的路线上的前驱站点到后驱站点的路径换乘次数必定为0)
        routeDirectionSave.forEach((routeID, direction) -> {
            // direction 为 -1或1 则单向建图, 否则为0双向建图
            List<StationTinyDTO> stationTinyDTOS = routePointSave.get(routeID);

            for(int i = 0; i < stationTinyDTOS.size(); i++)
                for(int j = i + 1; j < stationTinyDTOS.size(); j++) {
                    Long preStaID = stationTinyDTOS.get(i).getStaID();
                    Long curStaID = stationTinyDTOS.get(j).getStaID();
                    if(preStaID == -1 || curStaID == -1) continue;;
                    Long preUniqueTag = stationIDToUniqueTag.get(preStaID + ":" + routeID);
                    Long curUniqueTag = stationIDToUniqueTag.get(curStaID + ":" + routeID);
                    if(direction.get(0) != -1)
                        graph.insertEdge(preUniqueTag, curUniqueTag, 0);
                    if(direction.get(1) != -1)
                        graph.insertEdge(curUniqueTag, preUniqueTag, 0);
                }
        });


        stationBelongedRoutes.forEach((staID, routes) -> {
            if(routes.size() > 1) { // 存在需要于当前站点换乘的情况
                for(int i = 0; i < routes.size(); i++)
                    for(int j = 0; j < routes.size(); j++) {
                        if(i == j) continue; // 有在自己这一战下车,然后换乘到同一线路的反方向班次的情况,但是这一定会额外增加不必要的行程,和求解最短路的初衷相背
                        Long curRouteID = routes.get(i);
                        Long nextRouteID = routes.get(j);
                        Long curStation = stationIDToUniqueTag.get(staID + ":" + curRouteID);
                        Long nextStationInSameStaID = stationIDToUniqueTag.get(staID + ":" + nextRouteID);
                        // 从同路线建图中找出下一个站点集合(理论上,同一个路线只有两个方向,不考虑连续换乘最多只有两个next站点)
                        List<Edge<Integer>> edges = graph.getG().get(nextStationInSameStaID);
                        edges.forEach(edge -> {
                            Long nextStation = edge.getNextID();
                            String nextStationID = uniqueTagToStationID.get(nextStation);
                            Long routeID = Long.parseLong(nextStationID.split(":")[1]);
                            if(routeID.equals(nextRouteID)) { // 不考虑连续换乘,因此直觉上感觉需要保持在一条路线上,没有详细证明过
                                // 加边
                                if(nextStation > nextStationInSameStaID) { // 构建uniqueTag时,是同一线路按顺序插入,因此同一线路上正向时下一个站点序号大于前一站点序号,由此判断正反方向
                                    graph.insertEdge(curStation, nextStation, 1);
                                } else { // 反向
                                    graph.insertEdge(curStation, nextStation, 1);
                                }
                            }
                        });
                    }
            }
        });


        List<Long> startStationRoutes = stationBelongedRoutes.get(startStaID);
        List<Long> endStationRoutes = stationBelongedRoutes.get(endStaID);
        Integer ans = Integer.MAX_VALUE;
        List<List<Long>> ansPath = new ArrayList<>();
        for (Long startStationRoute : startStationRoutes)
            for (Long endStationRoute : endStationRoutes) {
                Long startStation = stationIDToUniqueTag.get(startStaID + ":" + startStationRoute);
                Long endStation = stationIDToUniqueTag.get(endStaID + ":" + endStationRoute);
                List<List<Long>> recordPath = BFSSearch(startStation, endStation, graph);
                if (graph.getRecordDistance() < ans) {
                    ans = graph.getRecordDistance();
                    ansPath = recordPath;
                } else if (graph.getRecordDistance().equals(ans)) {
                    ansPath.addAll(recordPath);
                }
            }

        return getSolutions(ans, ansPath, uniqueTagToStationID, cityID, routePointSave, graph);
    }
}
