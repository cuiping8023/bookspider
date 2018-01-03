package com.cienet.util;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by cuiping on 2018/1/3.
 */
@Component
public class BookDocumentAnalyzer implements DocumentAnalyzer{
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * 根据html文档对象获取List<Map>
     *
     * @param document html文档对象
     * @return 结果
     */
    @Override
    public List<Map<String, Object>> forListMap(Document document, Object info) {
        List<Map<String, Object>> results = new ArrayList<>();
        if (ObjectUtils.isEmpty(document))
            return results;
     Object obj = document.body();
       System.out.print( "body________________"+document.body() +"body end________________");
       System.out.println(document.body().getElementsByClass("subject-item"));
        document.body().getElementsByClass("subject-item").forEach(ele -> {
            try {
                Map<String, Object> result = new HashMap<>();
                Element tr = ele.nextElementSibling().child(1);
                Element a = tr.child(0).child(0).getElementsByTag("a").first();
                result.put("name", a.text());
               // result.put("id", a.attr("href").replaceAll("\\D+", ""));
                tr.getElementsByClass("rating_nums").get(0).text();
                Element div = tr.getElementsByClass("pl").get(0).parent();
                result.put("rate", Double.valueOf(div.getElementsByClass("rating_nums").get(0).text()));
                result.put("count", Integer.valueOf(div.getElementsByClass("pl").get(0).text().replaceAll("\\D+", "")));
                Element pub = tr.getElementsByClass("pub").first();
                result.put("publishingHouse",pub.text().replace("\\D+", ""));


                results.add(result);
            } catch (Exception ignored) {
            }
        });
        log.info("BookListDocumentAnalyzer::forListMap complete");
        return results;
    }
}
