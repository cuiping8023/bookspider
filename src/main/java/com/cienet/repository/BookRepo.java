package com.cienet.repository;

import com.cienet.po.Book;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by cuiping on 2018/1/3.
 */
public interface BookRepo extends JpaRepository<Book,Integer> {
}
