package org.seckill.dao;


import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.Seckill;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SeckillDaoTest {
	
	private SeckillDao seckillDao;
	
	@Resource
	public void setSeckillDao(SeckillDao seckillDao) {
		this.seckillDao = seckillDao;
	}


	@Test
	public void testQueryByid() throws Exception{
		long id = 1000;
		Seckill seckill = seckillDao.queryById(id);
		System.out.println(seckill.getName());
		System.out.println(seckill);
	}
	
	@Test
    public void queryAll() throws Exception {

        List<Seckill> seckills=seckillDao.queryAll(0,100);
        for (Seckill seckill : seckills)
        {
            System.out.println(seckill);
        }
    }
	
	@Test
    public void reduceNumber() throws Exception {

        long seckillId=1000;
        Date date=new Date();
        int updateCount=seckillDao.reduceNumber(seckillId,date);
        System.out.println(updateCount);

    }
	
}
