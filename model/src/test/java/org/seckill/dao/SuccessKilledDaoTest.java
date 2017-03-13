package org.seckill.dao;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.SuccessKilled;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKilledDaoTest {
	private SuccessKilledDao successKilledDao;
	@Resource
	public void setSuccessKilledDaoTest(SuccessKilledDao successKilledDao) {
		this.successKilledDao = successKilledDao;
	}
	
	@Test
	public void testInsertSuccessKilled(){
		long seckillId = 1001L;
		long userPhone = 18780615345L;
		successKilledDao.insertSuccessKilled(seckillId, userPhone);
	}
	@Test
	public void testQueryByIdWithSeckill(){
		long seckillId = 1001L;
		long userPhone = 18780615345L;
		SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
		System.out.println(successKilled);
		System.out.println(successKilled.getSeckill());
	}
}
