package com.youthguide.api.global.util;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Component
public class QnetResponseParser {

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    /** JSON 응답에서 item 목록을 찾아 반환 (구조가 달라도 첫 번째 객체 배열을 탐색) */
    public List<Map<String, Object>> extractItems(String json) {
        Object root = jsonMapper.readValue(json, Object.class);
        return find(root);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> find(Object node) {
        if (node instanceof List<?> list) {
            if (!list.isEmpty() && list.get(0) instanceof Map) {
                return (List<Map<String, Object>>) list;
            }
            for (Object o : list) {
                List<Map<String, Object>> r = find(o);
                if (!r.isEmpty()) return r;
            }
        } else if (node instanceof Map<?, ?> map) {
            Object item = map.get("item");
            if (item instanceof Map) {              // item이 1건이면 배열이 아닌 객체로 오는 경우
                return List.of((Map<String, Object>) item);
            }
            for (Object v : map.values()) {
                List<Map<String, Object>> r = find(v);
                if (!r.isEmpty()) return r;
            }
        }
        return List.of();
    }
}