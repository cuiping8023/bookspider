package com.cienet.Controller;

import com.cienet.po.Book;
import com.cienet.service.BookSpiderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by cuiping on 2018/1/3.
 */
@RestController
@RequestMapping("/douban/book")
public class BookController {
    private final Logger log = LoggerFactory.getLogger(getClass());
    @Resource
    private BookSpiderService bookSpiderService;


    @ResponseBody
    @GetMapping("/search")
    public List<Book> search() throws Exception {

                for(int i=0;i<981;i+=20) {
                    System.out.println(i);
                    bookSpiderService.spiderBookInfo(i);
                }
        return null;
    }




}
