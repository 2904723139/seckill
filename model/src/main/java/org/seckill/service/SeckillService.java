package org.seckill.service;

import java.util.List;

import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillExcepion;

/**
 * 业务接口:站在使用者的角度设计接口
 * 三个方面:方法定义力度，参数，返回类型（return 类型/异常）
 * 
 * @author bulala
 *
 */


public interface SeckillService {
	
	/**
	 * 查询所有秒杀记录
	 * @return List<Seckill>
	 */
	List<Seckill> getSeckillList();
    /**
     * 查询单个秒杀记录
     * @param seckillId
     * @return
     */
	Seckill getById(long seckillId);
	/**
	 * 秒杀开启时秒杀接口地址，
	 * 否则输出系统时间和秒杀时间
	 * @param seckillId
	 */
	Exposer exportSeckillUrl(long seckillId);
	/**
	 * 事务方法，执行秒杀
	 * @param seckillId
	 * @param userPhone
	 * @param md5
	 * @return
	 * @throws SeckillExcepion
	 * @throws RepeatKillException
	 * @throws SeckillCloseException
	 */
	SeckillExecution executeSeckill(long seckillId,long userPhone,String md5)
			throws SeckillExcepion,RepeatKillException,SeckillCloseException;
	/**
	 *执行存储过程
	 * @param seckillId
	 * @param userPhone
	 * @param md5
	 * @return
	 * @throws SeckillExcepion
	 * @throws RepeatKillException
	 * @throws SeckillCloseException
	 */
	SeckillExecution executeSeckillProcedure(long seckillId,long userPhone,String md5)
			throws SeckillExcepion,RepeatKillException,SeckillCloseException;
}
