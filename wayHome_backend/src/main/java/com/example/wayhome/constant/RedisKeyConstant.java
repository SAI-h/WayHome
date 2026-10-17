package com.example.wayhome.constant;

/**
 * Redis 缓存键常量定义，统一维护缓存键前缀，避免魔法值散落在业务代码中。
 *
 * @author WayHome
 */
public final class RedisKeyConstant {

    /** 路径规划结果缓存键前缀 */
    public static final String PATH_CAL_RESULT_PREFIX = "path_cal_result:";

    /** 城市列表缓存键 */
    public static final String CITY_LIST = "cityList";

    /** 线路名称列表缓存键 */
    public static final String ROUTE_NAME_LIST = "routeNameList";

    /** 地理坐标（Geo）缓存键 */
    public static final String GEO_PLACES = "places";

    /** 站点名称映射缓存键前缀 */
    public static final String STATION_MAP_PREFIX = "stationMap:staID:";

    /** 线路名称映射缓存键前缀 */
    public static final String ROUTE_MAP_BY_ID_PREFIX = "routeMap:routeID:";

    /** 线路名称映射缓存键前缀（按名称 + 城市） */
    public static final String ROUTE_MAP_BY_NAME_PREFIX = "routeMap:routeName:";

    /** 站点关联线路缓存键前缀 */
    public static final String ROUTE_STATION_PREFIX = "route:staID:";

    /** 城市下线路缓存键前缀 */
    public static final String ROUTE_CITY_PREFIX = "route:cityID:";

    /** 按名称查询线路缓存键前缀 */
    public static final String ROUTE_NAME_QUERY_PREFIX = "route:routeName:";

    /** 线路下站点列表缓存键前缀 */
    public static final String ROUTE_STATION_MAP_PREFIX = "routeStationMap:";

    /** 班次列表缓存键前缀 */
    public static final String SCHEDULE_ROUTE_PREFIX = "schedule:routeID:";

    /** 班次明细缓存键前缀 */
    public static final String SCHEDULE_PREFIX = "schedule:";

    /** 城市下站点缓存键前缀 */
    public static final String STATION_CITY_PREFIX = "station:cityID:";

    /** 按名称查询站点缓存键前缀 */
    public static final String STATION_NAME_QUERY_PREFIX = "station:staName:";

    private RedisKeyConstant() {
        throw new UnsupportedOperationException("常量类不允许实例化");
    }
}
