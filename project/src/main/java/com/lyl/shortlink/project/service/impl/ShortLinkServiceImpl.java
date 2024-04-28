package com.lyl.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.text.StrBuilder;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyl.shortlink.project.common.conventions.exception.ClientException;
import com.lyl.shortlink.project.common.conventions.exception.ServiceException;
import com.lyl.shortlink.project.common.enums.ValidDateTypeEnum;
import com.lyl.shortlink.project.dao.entity.*;
import com.lyl.shortlink.project.dao.mapper.*;
import com.lyl.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import com.lyl.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.lyl.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.lyl.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.lyl.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.lyl.shortlink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import com.lyl.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.lyl.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.lyl.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.lyl.shortlink.project.service.ShortLinkService;
import com.lyl.shortlink.project.util.HashUtil;
import com.lyl.shortlink.project.util.LinkUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.lyl.shortlink.project.common.constants.RedisCacheConstant.*;
import static com.lyl.shortlink.project.common.constants.ShortLinkConstant.AMAP_REMOTE_URL;
import static com.lyl.shortlink.project.util.LinkUtil.getActualIp;


@Service
@Slf4j
@RequiredArgsConstructor
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO>  implements ShortLinkService{

    private final RBloomFilter<String> shortLinkCachePenetrationBloomFilter;
    private final StringRedisTemplate stringRedisTemplate;
    private final ShortLinkGotoMapper shortLinkGotoMapper;
    private final LinkAccessStatsMapper linkAccessStatsMapper;
    private final LinkOsStatsMapper linkOsStatsMapper;

    private final LinkBrowserStatsMapper linkLocaleBrowserStatsMapper;
    private final LinkDeviceStatsMapper linkLocaleDeviceStatsMapper;
    private final LinkLocaleStatsMapper linkLocaleStatsMapper;
    private final LinkStatsTodayMapper linkStatsTodayMapper;
    private final LinkNetworkStatsMapper linkLocaleNetworkStatsMapper;


    private final RedissonClient redissonClient;

    @Value("${short-link.domain.default}")
    private String createShortLinkDefaultDomain;
    @Value("${short-link.amap.key}")
    private String statsLocaleAmapKey;

    @SneakyThrows
    @Override
    public void restoreUrl(String shortUri, ServletRequest request, ServletResponse response) {
        String serverName = request.getServerName();
        String serverPort = Optional.of(request.getServerPort())
                .filter(each -> !Objects.equals(each, 80))
                .map(String::valueOf)
                .map(each -> ":" + each)
                .orElse("");
        String fullShortUrl = serverName + serverPort + "/" + shortUri;

        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(originalLink)) {
//            shortLinkStats(buildLinkStatsRecordAndSetUser(fullShortUrl, request, response));
            shortLinkStats(fullShortUrl, request, response);
            ((HttpServletResponse) response).sendRedirect(originalLink);
            return;
        }
        boolean contains = shortLinkCachePenetrationBloomFilter.contains(fullShortUrl);
        if (!contains) {
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }
        // 进一步判断是否存在误判断
        String gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(gotoIsNullShortLink)) {
            ((HttpServletResponse) response).sendRedirect("/page/notfound");
            return;
        }
        RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            // 二次查看缓存是否存在
            originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(originalLink)) {
//                shortLinkStats(buildLinkStatsRecordAndSetUser(fullShortUrl, request, response));
                shortLinkStats(fullShortUrl, request, response);
                ((HttpServletResponse) response).sendRedirect(originalLink);
                return;
            }
            gotoIsNullShortLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isNotBlank(gotoIsNullShortLink)) {
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            // 缓存不存在，查询数据库
            LambdaQueryWrapper<ShortLinkGotoDO> linkGotoQueryWrapper = Wrappers.lambdaQuery(ShortLinkGotoDO.class)
                    .eq(ShortLinkGotoDO::getFullShortUrl, fullShortUrl);
            ShortLinkGotoDO shortLinkGotoDO = shortLinkGotoMapper.selectOne(linkGotoQueryWrapper);
            if (shortLinkGotoDO == null) {
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, shortLinkGotoDO.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, fullShortUrl)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getEnableStatus, 0);
            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
            if (shortLinkDO == null || (shortLinkDO.getValidDate() != null && shortLinkDO.getValidDate().before(new Date()))) {
                stringRedisTemplate.opsForValue().set(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl), "-", 30, TimeUnit.MINUTES);
                ((HttpServletResponse) response).sendRedirect("/page/notfound");
                return;
            }
            stringRedisTemplate.opsForValue().set(
                    String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                    shortLinkDO.getOriginUrl(),
                    LinkUtil.getLinkCacheValidTime(shortLinkDO.getValidDate()), TimeUnit.MILLISECONDS
            );
//            shortLinkStats(buildLinkStatsRecordAndSetUser(fullShortUrl, request, response));
            shortLinkStats(fullShortUrl, request, response);
            ((HttpServletResponse) response).sendRedirect(shortLinkDO.getOriginUrl());
        } finally {
            lock.unlock();
        }
    }
    @Override
    public ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam) {
        // 短链接接口的并发量有多少？如何测试？详情查看：https://nageoffer.com/shortlink/question
//        verificationWhitelist(requestParam.getOriginUrl());
        String shortLinkSuffix = generateSuffix(requestParam);
        String fullShortUrl = StrBuilder.create(createShortLinkDefaultDomain)
                .append("/")
                .append(shortLinkSuffix)
                .toString();
        ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                .domain(createShortLinkDefaultDomain)
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .createdType(requestParam.getCreatedType())
                .validDateType(requestParam.getValidDateType())
                .validDate(requestParam.getValidDate())
                .describe(requestParam.getDescribe())
                .shortUri(shortLinkSuffix)
                .enableStatus(0)
                .totalPv(0)
                .totalUv(0)
                .totalUip(0)
                .delTime(0L)
                .fullShortUrl(fullShortUrl)
                .favicon(getFavicon(requestParam.getOriginUrl()))
                .build();
        ShortLinkGotoDO linkGotoDO = ShortLinkGotoDO.builder()
                .fullShortUrl(fullShortUrl)
                .gid(requestParam.getGid())
                .build();
        try {
            // 短链接项目有多少数据？如何解决海量数据存储？详情查看：https://nageoffer.com/shortlink/question
            baseMapper.insert(shortLinkDO);
            // 短链接数据库分片键是如何考虑的？详情查看：https://nageoffer.com/shortlink/question
            shortLinkGotoMapper.insert(linkGotoDO);
        } catch (DuplicateKeyException ex) {
            // 首先判断是否存在布隆过滤器，如果不存在直接新增
            if (!shortLinkCachePenetrationBloomFilter.contains(fullShortUrl)) {
                shortLinkCachePenetrationBloomFilter.add(fullShortUrl);
            }
            throw new ServiceException(String.format("短链接：%s 生成重复", fullShortUrl));
        }
        // 项目中短链接缓存预热是怎么做的？详情查看：https://nageoffer.com/shortlink/question
        stringRedisTemplate.opsForValue().set(
                String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                requestParam.getOriginUrl(),
                LinkUtil.getLinkCacheValidTime(requestParam.getValidDate()), TimeUnit.MILLISECONDS
        );
        // 删除短链接后，布隆过滤器如何删除？详情查看：https://nageoffer.com/shortlink/question
        shortLinkCachePenetrationBloomFilter.add(fullShortUrl);
        return ShortLinkCreateRespDTO.builder()
                .fullShortUrl("http://" + shortLinkDO.getFullShortUrl())
                .originUrl(requestParam.getOriginUrl())
                .gid(requestParam.getGid())
                .build();
    }

    private String generateSuffix(ShortLinkCreateReqDTO requestParam) {
        int customGenerateCount = 0;
        String shorUri;
        while (true) {
            if (customGenerateCount > 10) {
                throw new ServiceException("短链接频繁生成，请稍后再试");
            }
            String originUrl = requestParam.getOriginUrl();
            originUrl += UUID.randomUUID().toString();
            // 短链接哈希算法生成冲突问题如何解决？详情查看：https://nageoffer.com/shortlink/question
            shorUri = HashUtil.hashToBase62(originUrl);
//            LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
//                    .eq(ShortLinkDO::getGid, requestParam.getGid())
//                    .eq(ShortLinkDO::getFullShortUrl, createShortLinkDefaultDomain + "/" + shorUri)
//                    .eq(ShortLinkDO::getDelFlag, 0);
//            ShortLinkDO shortLinkDO = baseMapper.selectOne(queryWrapper);
//            if (shortLinkDO == null) {
//                break;
//            }
            String string = StrBuilder.create(createShortLinkDefaultDomain)
                    .append("/")
                    .append(shorUri)
                    .toString();
            if (!shortLinkCachePenetrationBloomFilter.contains(string)) {
                break;
            }
            customGenerateCount++;
        }
        return shorUri;
    }


    @Override
    public ShortLinkCreateRespDTO createShortLinkByLock(ShortLinkCreateReqDTO requestParam) {
        return null;
    }

    @Override
    public ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateShortLink(ShortLinkUpdateReqDTO requestParam) {
//        verificationWhitelist(requestParam.getOriginUrl());
        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
                .eq(ShortLinkDO::getGid, requestParam.getOriginGid())
                .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                .eq(ShortLinkDO::getDelFlag, 0)
                .eq(ShortLinkDO::getEnableStatus, 0);
        ShortLinkDO hasShortLinkDO = baseMapper.selectOne(queryWrapper);
        if (hasShortLinkDO == null) {
            throw new ClientException("短链接记录不存在");
        }
        if(requestParam.getGid().equals(hasShortLinkDO.getGid())){
            LambdaUpdateWrapper<ShortLinkDO> updateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getGid, requestParam.getGid())
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .set(Objects.equals(requestParam.getValidDateType(), ValidDateTypeEnum.PERMANENT.getCode()),
                            ShortLinkDO::getValidDate, null)
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .set(ShortLinkDO::getOriginUrl, requestParam.getOriginUrl())
                    .eq(ShortLinkDO::getEnableStatus, 0);
            update(updateWrapper);
        }else{
            LambdaUpdateWrapper<ShortLinkDO> linkUpdateWrapper = Wrappers.lambdaUpdate(ShortLinkDO.class)
                    .eq(ShortLinkDO::getFullShortUrl, requestParam.getFullShortUrl())
                    .eq(ShortLinkDO::getGid, hasShortLinkDO.getGid())
                    .eq(ShortLinkDO::getDelFlag, 0)
                    .eq(ShortLinkDO::getDelTime, 0L)
                    .eq(ShortLinkDO::getEnableStatus, 0)
                    .set(ShortLinkDO::getDelFlag, 1)
                    .set(ShortLinkDO::getDelTime, System.currentTimeMillis());
            update(linkUpdateWrapper);
            ShortLinkDO shortLinkDO = ShortLinkDO.builder()
                    .domain(createShortLinkDefaultDomain)
                    .originUrl(requestParam.getOriginUrl())
                    .gid(requestParam.getGid())
                    .createdType(hasShortLinkDO.getCreatedType())
                    .validDateType(requestParam.getValidDateType())
                    .validDate(requestParam.getValidDate())
                    .describe(requestParam.getDescribe())
                    .shortUri(hasShortLinkDO.getShortUri())
                    .enableStatus(hasShortLinkDO.getEnableStatus())
                    .totalPv(hasShortLinkDO.getTotalPv())
                    .totalUv(hasShortLinkDO.getTotalUv())
                    .totalUip(hasShortLinkDO.getTotalUip())
                    .fullShortUrl(hasShortLinkDO.getFullShortUrl())
                    .favicon(getFavicon(requestParam.getOriginUrl()))
//                    .favicon(hasShortLinkDO.getFavicon())
                    .delTime(0L)
                    .build();
            baseMapper.insert(shortLinkDO);
        }
    }

    @Override
    public IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam) {
        IPage<ShortLinkDO> resultPage = baseMapper.pageLink(requestParam);
        return resultPage.convert(each -> {
            ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });
////        LambdaQueryWrapper<ShortLinkDO> queryWrapper = Wrappers.lambdaQuery(ShortLinkDO.class)
////                .eq(ShortLinkDO::getGid, requestParam.getGid())
////                .eq(ShortLinkDO::getEnableStatus, 0)
////                .eq(ShortLinkDO::getDelFlag, 0)
////                .orderByDesc(ShortLinkDO::getCreateTime);
////        IPage<ShortLinkDO> shortLinkDOIPage = baseMapper.selectPage(requestParam, queryWrapper);
//        IPage<ShortLinkDO> shortLinkDOIPage = baseMapper.pageLink(requestParam);
//        return shortLinkDOIPage.convert(each -> {
////            ShortLinkPageRespDTO result = BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
////            result.setDomain("http://" + result.getDomain());
////            return result;
//            return BeanUtil.toBean(each, ShortLinkPageRespDTO.class);
//        });
    }

    public void shortLinkStats(ShortLinkStatsRecordDTO statsRecord) {
//        Map<String, String> producerMap = new HashMap<>();
//        producerMap.put("statsRecord", JSON.toJSONString(statsRecord));
        // 消息队列为什么选用RocketMQ？详情查看：https://nageoffer.com/shortlink/question
//        shortLinkStatsSaveProducer.send(producerMap);
    }

    @Override
    public List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam) {
        QueryWrapper<ShortLinkDO> queryWrapper = Wrappers.query(new ShortLinkDO())
                .select("gid as gid, count(*) as shortLinkCount")
                .in("gid", requestParam)
                .eq("enable_status", 0)
                .eq("del_flag", 0)
                .eq("del_time", 0L)
                .groupBy("gid");
        List<Map<String, Object>> shortLinkDOList = baseMapper.selectMaps(queryWrapper);
        return BeanUtil.copyToList(shortLinkDOList, ShortLinkGroupCountQueryRespDTO.class);
    }

    @SneakyThrows
    private String getFavicon(String url) {
        URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();
        int responseCode = connection.getResponseCode();
        if (HttpURLConnection.HTTP_OK == responseCode) {
            Document document = Jsoup.connect(url).get();
            Element faviconLink = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (faviconLink != null) {
                return faviconLink.attr("abs:href");
            }
        }
        return null;
    }

    private void shortLinkStats(String url, ServletRequest request, ServletResponse response) {
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        AtomicBoolean uvFlag = new AtomicBoolean(false);
        Cookie cookie = null;
        if (ArrayUtil.isNotEmpty(cookies)) {
            cookie = Arrays.stream(cookies)
                    .filter(each -> "uv".equals(each.getName()))
                    .findFirst().orElse(null);

        }
        // 获取当前时间
        ZonedDateTime now = ZonedDateTime.now();

        // 计算当前小时结束时间
        ZonedDateTime hourEnd = now.withHour(now.getHour() + 1).withMinute(0).withSecond(0).withNano(0)
                .minusSeconds(1);

        // 计算剩余秒数
        Duration remainingTime = Duration.between(now, hourEnd);
        int hourOfDay = LocalTime.now().getHour();
        int dayOfWeek = LocalDate.now().getDayOfWeek().getValue();
        if (cookie == null){
            String uv = UUID.fastUUID().toString();
            cookie = new Cookie("uv", uv);
            cookie.setMaxAge(60 * 60 * 24 * 30);
            cookie.setPath(StrUtil.split(url, "/").get(1));
            cookie.setMaxAge((int) remainingTime.getSeconds());
            ((HttpServletResponse) response).addCookie(cookie);
            uvFlag.set(true);
        }
        String requestUtl = ((HttpServletRequest) request).getRequestURL().toString();
        Long size = stringRedisTemplate.opsForHyperLogLog().size(String.format(UIP_SHORT_LINK_KEY, url, hourOfDay));
        if (Boolean.TRUE.equals(stringRedisTemplate.hasKey(String.format(UIP_SHORT_LINK_KEY, url, hourOfDay)))) {
            stringRedisTemplate.opsForHyperLogLog().add(String.format(UIP_SHORT_LINK_KEY, url, hourOfDay), requestUtl);
        }else{
            stringRedisTemplate.opsForHyperLogLog().add(String.format(UIP_SHORT_LINK_KEY, url, hourOfDay), requestUtl);
            // 获取当前小时结束还有多少秒
            stringRedisTemplate.expire(String.format(UIP_SHORT_LINK_KEY, url, hourOfDay), remainingTime.getSeconds(), TimeUnit.SECONDS);
        }
        size = size - stringRedisTemplate.opsForHyperLogLog().size(String.format(UIP_SHORT_LINK_KEY, url, hourOfDay));
        Date date = new Date();
        LinkAccessStatsDO linkAccessStatsDO = LinkAccessStatsDO.builder()
                .pv(1)
                .uv(uvFlag.get() ? 1 : 0)
                .uip(size.intValue())
                .hour(hourOfDay)
                .fullShortUrl(url)
                .weekday(dayOfWeek)
                .date(date)
                .build();
        linkAccessStatsMapper.shortLinkStatsAccessRecord(linkAccessStatsDO);

        Map<String, Object> localeParamMap = new HashMap<>();
        localeParamMap.put("key", statsLocaleAmapKey);
        localeParamMap.put("ip", getActualIp((HttpServletRequest) request));
        String localeResultStr = HttpUtil.get(AMAP_REMOTE_URL, localeParamMap);
        JSONObject localeResultObj = JSON.parseObject(localeResultStr);
        String infoCode = localeResultObj.getString("infocode");
        String actualProvince = "未知";
        String actualCity = "未知";
        if (StrUtil.isNotBlank(infoCode) && StrUtil.equals(infoCode, "10000")) {
            String province = localeResultObj.getString("province");
            boolean unknownFlag = StrUtil.equals(province, "[]");
            LinkLocaleStatsDO linkLocaleStatsDO = LinkLocaleStatsDO.builder()
                    .province(actualProvince = unknownFlag ? actualProvince : province)
                    .city(actualCity = unknownFlag ? actualCity : localeResultObj.getString("city"))
                    .adcode(unknownFlag ? "未知" : localeResultObj.getString("adcode"))
                    .cnt(1)
                    .fullShortUrl(url)
                    .country("中国")
                    .date(date)
                    .build();
            linkLocaleStatsMapper.shortLinkLocaleState(linkLocaleStatsDO);
            }

        LinkOsStatsDO linkOsStatsDO = LinkOsStatsDO.builder()
                .fullShortUrl(url)
                .date(date)
                .cnt(1)
                .os(LinkUtil.getOs((HttpServletRequest) request))
                .build();
        linkOsStatsMapper.shortLinkOsState(linkOsStatsDO);


        LinkBrowserStatsDO linkBrowserStatsDO = LinkBrowserStatsDO.builder()
                .fullShortUrl(url)
                .date(date)
                .cnt(1)
                .browser(LinkUtil.getBrowser((HttpServletRequest) request))
                .build();
        linkLocaleBrowserStatsMapper.shortLinkBrowserState(linkBrowserStatsDO);

        LinkDeviceStatsDO linkDeviceStatsDO = LinkDeviceStatsDO.builder()
                .fullShortUrl(url)
                .date(date)
                .cnt(1)
                .device(LinkUtil.getDevice((HttpServletRequest) request))
                .build();
        linkLocaleDeviceStatsMapper.shortLinkDeviceState(linkDeviceStatsDO);

        LinkNetworkStatsDO linkNetworkStatsDO = LinkNetworkStatsDO.builder()
                .fullShortUrl(url)
                .date(date)
                .cnt(1)
                .network(LinkUtil.getNetwork((HttpServletRequest) request))
                .build();
        linkLocaleNetworkStatsMapper.shortLinkNetworkState(linkNetworkStatsDO);

        LinkAccessLogsDO linkAccessLogsDO = LinkAccessLogsDO.builder()
                .fullShortUrl(url)
                .user(cookie.getValue())
                .ip(getActualIp((HttpServletRequest) request))
                .os(linkOsStatsDO.getOs())
                .browser(linkBrowserStatsDO.getBrowser())
                .device(linkDeviceStatsDO.getDevice())
                .network(linkNetworkStatsDO.getNetwork())
                .locale(actualProvince + actualCity)
                .build();
        baseMapper.incrementStats("0", url, uvFlag.get() ? 1 : 0, 1, size.intValue());
        // TODO 需要重新判断
        LinkStatsTodayDO linkStatsTodayDO = LinkStatsTodayDO.builder()
                .todayPv(1)
                .todayUv(uvFlag.get() ? 1 : 0)
                .todayUip(size.intValue())
                .fullShortUrl(url)
                .date(new Date())
                .build();
        linkStatsTodayMapper.shortLinkTodayState(linkStatsTodayDO);
    }
}
