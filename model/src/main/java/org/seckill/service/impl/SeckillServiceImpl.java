package org.seckill.service.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dao.cache.RedisDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStateEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillExcepion;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

//@Component @Service @Dao @Conroller
@Service
public class SeckillServiceImpl implements SeckillService{

	private Logger logger = LoggerFactory.getLogger(this.getClass());

	private SeckillDao seckillDao;

	private SuccessKilledDao successKilledDao;

	private RedisDao redisDao;

	//md5盐值字符串，用于混淆MD5
	private final String slat = "adowqhodhqwoh&&#&$&YYHSYGSDTYGWjkfj454ABS";

	//注入Service依赖
	@Resource
	public void setSeckillDao(SeckillDao seckillDao) {
		this.seckillDao = seckillDao;
	}
	@Resource
	public void setSuccessKilledDao(SuccessKilledDao successKilledDao) {
		this.successKilledDao = successKilledDao;
	}

	@Resource
	public void setRedisDao(RedisDao redisDao) {
		this.redisDao = redisDao;
	}
	@Override
	public List<Seckill> getSeckillList() {
		// TODO Auto-generated method stub
		return seckillDao.queryAll(0, 4);
	}

	@Override
	public Seckill getById(long seckillId) {
		// TODO Auto-generated method stub
		return seckillDao.queryById(seckillId);
	}

	@Override
	public Exposer exportSeckillUrl(long seckillId) {
		//优化点：缓存优化,超时的基础上维护一致性
		//1.访问redis
		Seckill seckill = redisDao.getSeckill(seckillId);
		if(seckill == null){
			seckill = seckillDao.queryById(seckillId);
			if(seckill == null){
				return new Exposer(false, seckillId);
			}else{
				//放入redis
				redisDao.putSeckill(seckill);
			}
		}

		Date startTime = seckill.getStartTime();
		Date endTime = seckill.getEndTime();
		//当前系统时间
		Date nowTime = new Date();

		if(nowTime.getTime() < startTime.getTime()
				|| nowTime.getTime() > endTime.getTime()){
			return new Exposer(false, seckillId,nowTime.getTime(),startTime.getTime(),endTime.getTime());
		}
		//转换字符串过程，不可逆
		String md5 = getMD5(seckillId);
		return new Exposer(true,md5,seckillId);
	}

	@Override
	@Transactional
	/**
	 * 使用注解控制事务方法的优点
	 * 1：开发团队达成一致约定,明确标注事务方法的风格
	 * 2：保证事务方法的执行时间尽可能短，不要穿插其它网络操作RPC/HTTP请求或者剥离到事务方法外部
	 * 3：不是所有的方法都需要事务,如只有一条修改操作或者只读操作不需要事务控制
	 */
	public SeckillExecution executeSeckill(long seckillId, long userPhone, String md5)
			throws SeckillExcepion, RepeatKillException, SeckillCloseException {
		// TODO Auto-generated method stub
		if(md5==null || !md5.equals(getMD5(seckillId))){

			throw new SeckillExcepion("Seckill data rewrite");
		}
		//执行秒杀逻辑：减库存+记录购买行为
		Date killTime = new Date();
		try {
			//记录购买行为
			int insertCount = successKilledDao.insertSuccessKilled(seckillId, userPhone);
			//唯一：seckillId,userPhone
			if(insertCount<=0){
				//重复秒杀
				throw new RepeatKillException("seckill repeated");
			}else{ 
				//减库存，热点商品竞争
				int updateCount = seckillDao.reduceNumber(seckillId, killTime);
				if(updateCount<=0){
					//没有更新记录，秒杀结束rollback
					throw new SeckillCloseException("seckill closed");
				}else{
					//秒杀成功commit
					SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
					return new SeckillExecution(seckillId,SeckillStateEnum.SUCCESS,successKilled);					
				}
			}
			
		}catch (SeckillCloseException e) {
			// TODO: handle exception
			throw e;
		}catch (RepeatKillException e) {
			// TODO: handle exception
			throw e;
		} catch (Exception e) {
			// TODO: handle exception
			logger.error(e.getMessage(),e);
			//所有编译期异常，转化成运行期异常
			throw new SeckillCloseException("seckill inner error"+e.getMessage());
		}
	}
	private String getMD5(long seckillId){
		String base = seckillId +"/"+ slat;
		String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
		return md5;
	}
	@Override
	public SeckillExecution executeSeckillProcedure(long seckillId, long userPhone, String md5)
			throws SeckillExcepion, RepeatKillException, SeckillCloseException {
		if(md5 == null || !md5.equals(getMD5(seckillId))){
			return new SeckillExecution(seckillId, SeckillStateEnum.DATA_REWRITE);
		}
		Date killTime = new Date();
		Map<String, Object> paramMap = new HashMap<String, Object>();
		paramMap.put("seckillId", seckillId);
		paramMap.put("phone", userPhone);
		paramMap.put("killTime", killTime);
		paramMap.put("result", null);
		//执行完存储过程后result被赋值
		try {
			seckillDao.killByProcedure(paramMap);
			//获取result
		 int result = MapUtils.getInteger(paramMap, "result",-2);
		 if(result == 1){
			 SuccessKilled sk = successKilledDao.queryByIdWithSeckill(seckillId, userPhone);
			 return new SeckillExecution(seckillId, SeckillStateEnum.SUCCESS,sk);
		 }else {
			return new SeckillExecution(seckillId, SeckillStateEnum.stateOf(result));
		}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
			return new SeckillExecution(seckillId, SeckillStateEnum.INNER_ERROR);
		}
		
	}

}
