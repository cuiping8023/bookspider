package com.cienet.service;

import com.cienet.po.Book;

import java.util.List;

/**
 * Created by cuiping on 2018/1/3.
 */
public interface BookSpiderService {
    List<Book> spiderBookInfo(int start) throws Exception;
}
