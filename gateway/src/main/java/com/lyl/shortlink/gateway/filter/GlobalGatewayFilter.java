package com.lyl.shortlink.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
//@Deprecated
@Slf4j
public class GlobalGatewayFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 打印请求信息
        log.info("请求路径：{}", exchange.getRequest().getPath());
        return chain.filter(exchange);

//        ServerHttpRequest request = exchange.getRequest();
//        PathContainer pathContainer = request.getPath().pathWithinApplication();
//        // 添加gatewayKey，防止下游接口直接被访问
//        ServerHttpRequest.Builder mutate = request.mutate();
//        mutate.header("secretKey", "key");
//        return chain.filter(exchange.mutate().request(mutate.build()).build());
    }
}
