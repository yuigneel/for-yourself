package com.yulgnier.gateway.config;

import cn.hutool.json.JSONUtil;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.yulgnier.gateway.config.properties.DynamicRouteLoaderProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionWriter;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;

@Slf4j
@Component
@RequiredArgsConstructor
public class DynamicRouteLoaderConfig {
    private final NacosConfigManager nacosConfigManager;
    private final RouteDefinitionWriter routeDefinitionWriter; //更新路由表工具
    private final DynamicRouteLoaderProperties dynamicRouteLoaderProperties;
    private final Set<String> routeIds = new HashSet<>();

    @PostConstruct
    public void initRouteConfigListener() throws NacosException {
        log.info("========== 开始初始化动态路由配置 ==========");
        //1.项目启动时，先拉取一次配置，然后监听配置的更新
        String configInfo = nacosConfigManager.getConfigService().getConfigAndSignListener(dynamicRouteLoaderProperties.getDataId(), dynamicRouteLoaderProperties.getGoroup(), 5000, new Listener() {
            /**
             *  获取线程池 目前用不到
             */
            @Override
            public Executor getExecutor() {
                return null;
            }

            @Override
            public void receiveConfigInfo(String configInfo) {
                log.info("========== 监听到路由配置更新 ========== ");
                log.info("新的配置信息：{}", configInfo);
                //2.监听配置的更新，需要跟新路由表
                updateRouteConfigInfo(configInfo);
            }
        });
        log.info("首次从 Nacos 拉取的路由配置：{}", configInfo);
        //3.第一次获取配置，也需要跟新路由表
        updateRouteConfigInfo(configInfo);
        log.info("========== 动态路由初始化完成 ==========");
    }

    //4.跟新路由表
    public void updateRouteConfigInfo(String configInfo) {
        log.info("开始更新路由表...");
        try {
            //1. 解析 json 配置信息，转为 RouteDefinition
            List<RouteDefinition> routeDefinitions = JSONUtil.toList(configInfo, RouteDefinition.class);
            log.info("解析到路由配置数量：{}", routeDefinitions.size());

            //2. 删除路由表
            log.info("开始清除旧路由，待清除数量：{}", routeIds.size());
            routeIds.forEach(routeId -> {
                log.debug("正在删除路由：{}", routeId);
                routeDefinitionWriter.delete(Mono.just(routeId)).subscribe();
            });
            routeIds.clear();
            log.info("旧路由清除完成");

            //3. 更新路由表
            log.info("开始加载新路由，数量：{}", routeDefinitions.size());
            routeDefinitions.forEach(routeDefinition -> {
                try {
                    routeDefinitionWriter.save(Mono.just(routeDefinition)).subscribe();
                    //3.1 添加路由表 id
                    routeIds.add(routeDefinition.getId());
                    log.info("路由加载成功：ID={}, URI={}", routeDefinition.getId(), routeDefinition.getUri());
                } catch (Exception e) {
                    log.error("更新路由表失败：{}", e.getMessage(), e);
                }
            });
            log.info("路由表更新完成，当前路由总数：{}", routeIds.size());
        } catch (Exception e) {
            log.error("更新路由表过程中发生异常：{}", e.getMessage(), e);
            throw e;
        }
    }
}
