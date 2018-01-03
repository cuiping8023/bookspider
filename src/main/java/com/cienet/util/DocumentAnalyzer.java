package com.cienet.util;

import org.jsoup.nodes.Document;

import java.util.List;
import java.util.Map;

/** html解析
 * Created by cuiping on 2018/1/3.
 */
public interface DocumentAnalyzer {
    /**
     * 根据html文档对象获取List<Map>
     *
     * @param document html文档对象
     * @return 结果
     */
    default List<Map<String, Object>> forListMap(Document document) {
        return forListMap(document, null);
    }

    List<Map<String, Object>> forListMap(Document document, Object info);
}
