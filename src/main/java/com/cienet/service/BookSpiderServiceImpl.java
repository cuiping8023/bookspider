package com.cienet.service;

import com.cienet.po.Book;
import com.cienet.repository.BookRepo;
import com.cienet.util.DocumentAnalyzer;
import com.cienet.util.SpiderUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by cuiping on 2018/1/3.
 */
@Service
public class BookSpiderServiceImpl implements  BookSpiderService {
    //当前任务数
    private AtomicInteger countTask = new AtomicInteger();

    //用于防止多个线程同时执行
    private AtomicLong lastTime = new AtomicLong();

    //当前网页的cookies
    private volatile Map<String, String> cookiesOfDouban = new HashMap<>();

    //最小间隔
    private static final int MIN_INTERVAL = 999;

    //最大抓取数
    private static final int MAX_COUNT = 999999;

    //最大线程数
    private static final int MAX_THREAD = 64;

    //key前缀
    private static final String REDIS_KEY_PRE_MOVIE = "douban_movie_ongoing";
    @Resource
    private BookRepo bookRepo;

    @Resource
    private DocumentAnalyzer documentAnalyzer;

    @Value("${url.douban.book}")
    private String preUrlOfSearch;

    private final Logger log = LoggerFactory.getLogger(getClass());

    @Override
    public List<Book> spiderBookInfo(int start) throws Exception {
        List<Book> result = SpiderUtil.forEntityList(preUrlOfSearch+"编程?start="+start+"&type=T", documentAnalyzer, Book.class);
        result.forEach(book -> {
          //  if (!bookRepo.exists(movieEntity.getId()))
                bookRepo.save(book);
        });
        return result;
    }



    /**
     * 数据抓取的进度信息
     */
    public static class ProcessingInfo implements Serializable {
        final static int PAGE_SIZE = 20;
        //总条数
        volatile int count;
        //当前抓取的位置
        volatile int currentIndex;
        //是否完成
        volatile boolean complete;

        public ProcessingInfo() {
        }

        @JsonIgnore
        public boolean isFinish() {
            return currentIndex >= count || currentIndex > MAX_COUNT;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }

        public void setCurrentIndex(int currentIndex) {
            this.currentIndex = currentIndex;
        }

        @JsonIgnore
        public int getAndIncreaseCurrentIndex() {
            int current = currentIndex;
            currentIndex += PAGE_SIZE;
            return current;
        }

        public boolean isComplete() {
            return complete;
        }

        public void setComplete() {
            this.complete = true;
        }

        public int getCurrentIndex() {
            return currentIndex;
        }

        public void setComplete(boolean complete) {
            this.complete = complete;
        }
    }

    //登录
    private void doLogin() {
        doLogin(null);
    }

    private void doLogin(String code) {
        log.info("ReviewingServiceImpl::doLogin");
        try {
            cookiesOfDouban = Jsoup.connect("https://accounts.douban.com/login").headers(header()).method(Connection.Method.POST).data(params(code)).execute().cookies();
        } catch (IOException e) {
            log.info("ReviewingServiceImpl::doLogin IOException");
            e.printStackTrace();
        }
    }

    private void imNotRobot(String robot) {
        log.info("ReviewingServiceImpl::imNotRobot");
        try {
            Map<String, String> data = new HashMap<>();
            String[] params = robot.split(",");
            data.put("ck", "cKqf");
            data.put("captcha-solution", params[0]);
            data.put("captcha-id", params[1]);
            data.put("original-url", "https%253A%252F%252Fmovie.douban.com%252F");
            cookiesOfDouban = Jsoup.connect("https://accounts.douban.com/login").headers(header()).method(Connection.Method.POST).data(data).execute().cookies();
        } catch (IOException e) {
            log.info("ReviewingServiceImpl::imNotRobot IOException");
            e.printStackTrace();
        }
    }

    //获取登陆验证码
    private String[] getCheckImg() {
        log.info("ReviewingServiceImpl::getCheckImg get the check image");
        String[] result = new String[3];
        //captcha_image
        try {
            Element body = Jsoup.connect("https://accounts.douban.com/login").get().body();
            //判断是否需要验证码登陆
            Element img = body.getElementById("captcha_image");
            if (img != null) {//captcha-id
                result[0] = "login";
                result[1] = img.attr("src");
                return result;
            }
            //首先判断是否需要验证机器人
            Elements imgRobots = body.getElementsByAttributeValue("alt", "captcha");
            if (imgRobots.size() > 0 && imgRobots.get(0) != null) {
                Element imgRobot = imgRobots.get(0);
                result[0] = "robot";
                result[1] = imgRobot.attr("src");
                result[2] = body.getElementsByAttributeValue("name", "captcha-id").get(0).val();
                return result;
            }
        } catch (IOException e) {
            log.info("ReviewingServiceImpl::getCheckImg IOException");
            e.printStackTrace();
        }
        return null;
    }

    //登陆需要的header
    private Map<String, String> header() {
        Map<String, String> header = new HashMap<>();
        header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        header.put("Accept-Encoding", "gzip, deflate, sdch, br");
        header.put("Accept-Language", "zh-CN,zh;q=0.8");
        header.put("Connection", "keep-alive");
        header.put("Host", "movie.douban.com");
        header.put("Referer", "https://movie.douban.com/explore");
        header.put("Upgrade-Insecure-Requests", "1");
        header.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/57.0.2987.133 Safari/537.36");
        return header;
    }

    private Map<String, String> params(String code) {
        Map<String, String> header = new HashMap<>();
        header.put("source", "movie");
        header.put("redir", "https://movie.douban.com/");
        header.put("form_email", "cuiping0515@163.com");
        header.put("form_password", "515a116a");
        if (StringUtils.hasText(code))
            header.put("captcha-solution", code);
        header.put("login", "登录");
        return header;
    }
}
