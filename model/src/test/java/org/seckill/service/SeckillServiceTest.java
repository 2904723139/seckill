package org.seckill.service;

import java.util.List;

import javax.annotation.Resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml",
	                   "classpath:spring/spring-service.xml"})
public class SeckillServiceTest {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
	private SeckillService seckillService;
    
	@Resource
	public void setSeckillService(SeckillService seckillService) {
		this.seckillService = seckillService;
	}
	
	@Test
	public void testGetSeckillList() throws Exception{
		List<Seckill> list = seckillService.getSeckillList();
		logger.info("list={}",list);
	}
	@Test
	public void testGetById() throws Exception{
		long seckillId = 1000;
		Seckill seckill = seckillService.getById(seckillId);
		logger.info("seckill={}", seckill);
	}
	@Test
	public void testSeckillLogic()throws Exception{
		long seckillId = 1002;
		Exposer exposer = seckillService.exportSeckillUrl(seckillId);
		if(exposer.isExposed()){
			logger.info("exposer={}", exposer);
			long phone=18080485343L;
			String md5 = exposer.getMd5();
			try {
				SeckillExecution execution = seckillService.executeSeckill(seckillId, phone, md5);
				logger.info("result={}",execution);
				
			} catch (RepeatKillException e) {
				// TODO: handle exception
				logger.error(e.getMessage());
			}catch (SeckillCloseException e) {
				// TODO: handle exception
				logger.error(e.getMessage());
			}
		}else{
			logger.warn("exposer={}",exposer);
		}
	}
	@Test
	public void testExecuteSeckillProcedure()throws Exception{
		long seckillId = 1001;
		long phone = 18080485346l;
		Exposer exposer = seckillService.exportSeckillUrl(seckillId);
		if(exposer.isExposed()){
			String md5 = exposer.getMd5();
			SeckillExecution seckillExecution = seckillService.executeSeckillProcedure(seckillId, phone, md5);
			logger.info(seckillExecution.getStateInfo());
		}
	}
}
