package com.cienet.util;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by cuiping on 2018/1/3.
 */
public class SpiderUtil {

    private static final Logger log = LoggerFactory.getLogger(TransUtil.class);

    public static <T> List<T> forEntityList(String url, DocumentAnalyzer docAnalyzer, Class<T> type) throws Exception {
        return forEntityList(url, docAnalyzer, type, null, null);
    }

    public static <T> List<T> forEntityList(String url, DocumentAnalyzer docAnalyzer, Class<T> type, Object info, Map<String, String> cookies) throws Exception {

        log.info("spider is working ：" + url);

        List<T> results = new ArrayList<>();
        Connection connection = Jsoup.connect(url).timeout(50000);
        //设置请求头
        if (cookies != null && cookies.size() > 0)
            connection.cookies(cookies);
        Connection.Response response = connection.execute();

        docAnalyzer.forListMap(response.parse(), info).forEach(map -> {
            try {
                results.add(TransUtil.mapToBean(map, type));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        return results;
    }

    private static Map<String, String> mergeMap(Map<String, String> base, Map<String, String> more) {
        more.forEach(base::put);
        more = base;
        return more;
    }
}
