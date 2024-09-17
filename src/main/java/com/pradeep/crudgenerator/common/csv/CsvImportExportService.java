package com.pradeep.crudgenerator.common.csv;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.CsvMapWriter;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.io.ICsvMapWriter;
import org.supercsv.prefs.CsvPreference;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class CsvImportExportService {

    public <T> List<T> readCsvFile(MultipartFile file, Class<T> clazz, String[] header, CellProcessor[] processors) throws IOException {
        List<T> objList = new ArrayList<T>();
        try (ICsvBeanReader beanReader = new CsvBeanReader(new InputStreamReader(file.getInputStream()),
                CsvPreference.STANDARD_PREFERENCE)) {
            T obj;
            beanReader.getHeader(true);
            while ((obj = beanReader.read(clazz, header, processors)) != null) {
                objList.add(obj);
            }
        }
        return objList;
    }

    public <T> Resource generateCsvFile(List<T> clazz, List<String> noNeededColumn) throws IOException {
        List<Method> methods = getMethodsList(clazz, noNeededColumn);
        String[] extractHeader = getHeader(methods);

        StringWriter st = new StringWriter();
        try (ICsvMapWriter mapWriter = new CsvMapWriter(st, CsvPreference.EXCEL_PREFERENCE)) {
            mapWriter.writeHeader(extractHeader);
            for (final T obj : clazz) {
                Map<String, Object> mapPrices = buildMapLine(methods, obj);
                mapWriter.write(mapPrices, extractHeader, new CellProcessor[extractHeader.length]);
            }
        }
        return (new InputStreamResource(new ByteArrayInputStream(st.toString().getBytes(StandardCharsets.UTF_8))));
    }


    private <T> Map<String, Object> buildMapLine(List<Method> methods, T obj) {
        Map<String, Object> output = new HashMap<>();
        for (Method method : methods) {
            try {
                output.put(getColumnName(method), obj.getClass().getMethod(method.getName()).invoke(obj));
            } catch (IllegalAccessException | IllegalArgumentException |
                     InvocationTargetException
                     | NoSuchMethodException | SecurityException e) {
                log.error(e.getLocalizedMessage());
            }
        }
        return output;
    }


    private <T> List<Method> getMethodsList(List<T> clazz, List<String> noNeededColumn) {
        return Arrays.stream(clazz.get(0).getClass().getMethods())
                .parallel()
                .filter(method -> (method.getName().startsWith("get") || method.getName().startsWith("is"))
                        && !"getClass".equals(method.getName())
                        && !noNeededColumn.contains(getColumnName(method)))
                .sorted(Comparator.comparing(this::getColumnName))
                .collect(Collectors.toList());
    }

    private String[] getHeader(List<Method> methods) {
        return methods.stream()
                .map(this::getColumnName)
                .toArray(String[]::new);
    }

    private String getColumnName(Method method) {
        if (method.getName().startsWith("get"))
            return StringUtils.uncapitalize(method.getName().substring(3));
        else if (method.getName().startsWith("is"))
            return StringUtils.uncapitalize(method.getName().substring(2));
        else
            return "";
    }
}
