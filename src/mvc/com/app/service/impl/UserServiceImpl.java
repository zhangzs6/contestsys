/** 
 * @author lxm
 * @create_date 2019.5.3
 * @description 用户管理服务
 * */
package com.app.service.impl;

import java.io.UnsupportedEncodingException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.mail.MessagingException;

import com.annotation.SystemServiceLog;
import com.app.service.UserService;
import com.app.tools.MD5Util2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.app.dao.ClassMapper;
import com.app.dao.CourseMapper;
import com.app.dao.UserMapper;
import com.app.tools.MD5Util;
import com.app.tools.RandomString;
import com.code.model.User;
import com.code.model.UserExample;
import com.github.pagehelper.PageHelper;

import net.sf.json.JSONArray;


@Service 
public class UserServiceImpl implements UserService {

	@Autowired
	private UserMapper userDao;
	
	@Autowired
	private ClassMapper classDao;
	
	@Autowired
	private CourseMapper courseDao;
	
	@Autowired
    private JavaMailSender javaMailSender;
	
	public int LoginByUserName(String userName,String passWord) throws UnsupportedEncodingException, MessagingException, GeneralSecurityException
	{
		UserExample userExample = new UserExample();
		UserExample.Criteria criteria= userExample.createCriteria();
		criteria.andUserIdEqualTo(userName);

		List<User> list = userDao.selectByExample(userExample);
		//System.out.println("size=" + list.size());
	    if(list.size()==1)
	    {
	    	User user = list.get(0);
	    	if(user.getState()==0 )  //待激活
    		{
	    		if(user.getValidatetime().before(new Date()))
	    		{
	    			User record = new User();
	        		Calendar ca = Calendar.getInstance();
	        		ca.setTime(new Date());
	        		ca.add(Calendar.MINUTE, 15);
	        		record.setValidatetime(ca.getTime());
	        		record.setValidatecode(MD5Util.encryption(RandomString.getRandomString(32)));
	        		userDao.updateByExampleSelective(record, userExample);
	        		ApplyActivate(user.getEmail(),user.getValidatecode());
	    		}
    		
    		return -1;
    		}
			// 根据用户名查询用户信息
	    	list = userDao.selectByExample(userExample);
			System.out.println("user=="+list);
	    	if(list.size()==1) {
	    		user = list.get(0);
	    		String selPassword = user.getPassword();
	    		if (MD5Util2.getSaltverifyMD5(passWord,selPassword)) {
					if(user.getState()<0)
						return -1; //状态异常（封禁状态）
					else if(user.getLevel()==2)
						return 4;  //管理员
					else if(user.getLevel()==1)
						return 3;   //教师用户
					else return 2; //普通用户:学生
				} else {
	    			return 1;
				}
	    	}
	    	else return 0; //用户名不存在
	    		
	    }
	    else return 0;  //用户名不存在
	    
	}
	
	public int LoginByEmail(String email,String passWord) throws UnsupportedEncodingException, MessagingException, GeneralSecurityException
	{
		UserExample userExample = new UserExample();
		UserExample.Criteria criteria= userExample.createCriteria();
		criteria.andEmailEqualTo(email);

		List<User> list = userDao.selectByExample(userExample);
	    if(list.size()==1) {
	    	User user = list.get(0);
	    	if(user.getState()==0  ) {
	    		if(user.getValidatetime().before(new Date())) {
	    			User record = new User();
		    		Calendar ca = Calendar.getInstance();
		    		ca.setTime(new Date());
		    		ca.add(Calendar.MINUTE, 15);

		    		record.setValidatetime(ca.getTime());
		    		record.setValidatecode(MD5Util.encryption(RandomString.getRandomString(32)));

		    		userDao.updateByExampleSelective(record, userExample);
		    		ApplyActivate(user.getEmail(),user.getValidatecode());
		    		
	    		}
	    		
	    		return -1;
	    	}
			// 根据email查询用户信息
			list = userDao.selectByExample(userExample);
			if(list.size()==1) {
				user = list.get(0);
				String selPassword = user.getPassword();
				if (MD5Util2.getSaltverifyMD5(passWord,selPassword)) {
					if(user.getState()<0)
						return -1; //状态异常（封禁状态）
					else if(user.getLevel()==2)
						return 4;  //管理员
					else if(user.getLevel()==1)
						return 3;   //教师用户
					else return 2; //普通用户:学生
				} else {
					return 1;
				}
			}
			else return 0; //用户名/邮箱不存在
	    		
	    }
	    else return 0;  //用户名/邮箱不存在
	}

	/**
	 * 注册接口
	 * @param user
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws MessagingException
	 * @throws GeneralSecurityException
	 */
	@Transactional(rollbackFor=Exception.class)		// 回滚注解，抛出异常自动回滚
	public String Register(User user) throws UnsupportedEncodingException, MessagingException, GeneralSecurityException
	{
		String userNameRegex = "^[0-9]*$";
		String passWordRegex = "^[A-Za-z0-9]+$";
		String emailRegex= "^[0-9A-Za-z][\\.-_0-9A-Za-z]*@[0-9A-Za-z]+(\\.[0-9A-Za-z]+)+$";
		
		if(user.getUserId()==null || user.getUserId().length()!=13 || !Pattern.matches(userNameRegex, user.getUserId()))
			return "账号/学号不符合规范（要求13位且全部为数字）";
		if(user.getPassword()==null || !(user.getPassword().length()>=8 && user.getPassword().length()<=16) || !Pattern.matches(passWordRegex, user.getPassword()))
			return "密码不符合规范（要求8-16位且由数字或字母组成）";
		if(user.getEmail()==null || !Pattern.matches(emailRegex, user.getEmail()))
			return "邮箱格式错误";
		
		if(userDao.selectByPrimaryKey(user.getUserId())!=null)
			return "账号已经存在";
		if(userDao.selUserByEmail(user.getEmail()).size()>0)
			return "邮箱地址已经被使用";
		// MD5加盐加密
		String password = user.getPassword();
		password = MD5Util2.getSaltMD5(password);
		user.setPassword(password);
		//System.out.println("注册pss==="+password);


		user.setRegistertime(new Date());
		String validateCode = MD5Util.encryption(RandomString.getRandomString(32));
		user.setValidatecode(validateCode);
		Calendar ca = Calendar.getInstance();
		ca.setTime(new Date());
		ca.add(Calendar.MINUTE, 15);	//将时间延后15分钟
		user.setValidatetime(ca.getTime());
		userDao.insertSelective(user);

		ApplyActivate(user.getEmail(),user.getValidatecode());   //邮箱激活
		
		return "注册成功,请登录邮箱进行账号激活！";
	}

	/**
	 * 登录主接口
	 * @param userName
	 * @param passWord
	 * @return
	 * @throws UnsupportedEncodingException
	 * @throws MessagingException
	 * @throws GeneralSecurityException
	 */
	public int Login(String userName,String passWord) throws UnsupportedEncodingException, MessagingException, GeneralSecurityException
	{
		int state = LoginByEmail(userName,passWord);

		if(state==0) {
			return LoginByUserName(userName,passWord);
		} else {
			return state;
		}

	}
	
	public User GetUser(String username){
		return userDao.selectByPrimaryKey(username);
	}
	
	public String ProcessActivate(String email,String validateCode) throws UnsupportedEncodingException
	{
		UserExample userExample = new UserExample();
        UserExample.Criteria criteria= userExample.createCriteria();
        criteria.andEmailEqualTo(email);
        List<User> list = userDao.selectByExample(userExample);
        if(list.size()==1)
        {
        	User user = list.get(0);
        	if(user.getValidatecode().equals(validateCode))
        	{
        		Date date = user.getValidatetime();
        		System.out.println(date.toString());
        		if(date.after(new Date()))
        		{
        			user = new User();
        			user.setState(new Integer(1));
        			userDao.updateByExampleSelective(user, userExample);
        			return "激活成功";
        		}
        		else return "激活码过期";
        	}
        	else return "激活码错误";
        }
        else return "邮箱地址错误";
		
	}
	
	public void ApplyActivate(String email,String validateCode) throws UnsupportedEncodingException, MessagingException, GeneralSecurityException
	{
		
		StringBuilder sb = new StringBuilder();
		sb.append("您好:\n");
		sb.append("以下为您的邮箱激活地址：\n");
		
		sb.append("<a href=\"http://114.55.143.216:8080/contestsys/User/Validate?email=");

        sb.append(email); 
        sb.append("&validatecode=");  
        sb.append(validateCode);
        sb.append("\">请在15分钟内点击此链接激活您的账号");  
        sb.append("</a>");
        
        SimpleMailMessage message = new SimpleMailMessage();
        //发件人的邮箱地址
        message.setFrom("2267359448@qq.com");
        //收件人的邮箱地址
        message.setTo(email);
        //邮件主题
        message.setSubject("学生在线考试系统账号激活");
        //邮件内容
        message.setText(sb.toString());
        //发送邮件
        javaMailSender.send(message);
		//SendEmail.sendMessage("考试系统", email, "注册激活", sb.toString(), "text/html;charset=gb2312");
	}
	
	public String ApplyResetPassword(String email) throws UnsupportedEncodingException, MessagingException, GeneralSecurityException
	{
	    UserExample userExample = new UserExample();
	    UserExample.Criteria criteria = userExample.createCriteria();
	    criteria.andEmailEqualTo(email);
	    List<User> list = userDao.selectByExample(userExample);
	    if(list.size()==1)
	    {
	    	if(list.get(0).getState()<=0)  //状态异常
	    	return "账号状态异常";
	    	String ran = RandomString.getRandomNum(6);
	    	
	    	Calendar ca  = Calendar.getInstance();
	    	ca.setTime(new Date());
	    	ca.add(Calendar.MINUTE, 10);
	    	User user = new User();
	    	user.setValidatecode(ran);
	    	user.setValidatetime(ca.getTime());
	    	userDao.updateByExampleSelective(user, userExample);
	    	
	    	StringBuilder sb = new StringBuilder();
			sb.append("您好:\n");
			sb.append("以下为您的重置密码，其验证码为：");
	        sb.append(ran);
	        sb.append("\n如果不是您本人操作，请忽略此邮件");
	        
	        SimpleMailMessage message = new SimpleMailMessage();
	        //发件人的邮箱地址
	        message.setFrom("2267359448@qq.com");
	        //收件人的邮箱地址
	        message.setTo(email);
	        //邮件主题
	        message.setSubject("学生在线考试系统密码重置");
	        //邮件内容
	        message.setText(sb.toString());
	        //发送邮件
	        javaMailSender.send(message);
	    	
	    }
	    else return "邮箱地址不存在";
	      
		return "申请成功";
	}
	
	public String ResetPassword(String email , String newPassword ,String validateCode) throws UnsupportedEncodingException, MessagingException, GeneralSecurityException
	{
		String passWordRegex = "^[A-Za-z0-9]+$";
		int state = LoginByEmail(email,"");
	    if(state==-1) return "邮箱地址不存在";
	    else if(state==0) return "邮箱地址尚未激活，请到您的邮箱进行激活";
	    
	    UserExample userExample = new UserExample();
	    UserExample.Criteria criteria = userExample.createCriteria();
	    criteria.andEmailEqualTo(email);
	    List<User> list = userDao.selectByExample(userExample);
	    User user = list.get(0);
	    if(user.getValidatecode().equals(validateCode))
	    {
	    	if(user.getValidatetime().after(new Date()))
	    	{
	    		user = new User();
	    		if(Pattern.matches(passWordRegex, newPassword))
	    		{
					// MD5加盐加密
					String password = user.getPassword();
					password = MD5Util2.getSaltMD5(newPassword);

	    			user.setPassword(password);

		    		userDao.updateByExampleSelective(user, userExample);
		    		return "密码重置成功";
	    		}
	    		else return "密码不符合规范（要求8-16位且由数字或字母组成）";
	    	}
	    		else return "验证码过期";	
	    }
	    else return "验证码错误";
	    
	}
	
	public List GetAllStudents(String Keyword,String pageSize,String pageNumber){
        System.out.println("key=" + Keyword);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		
		//分页所需相关参数的计算
		if(pageSize!=null&&pageNumber!=null) {
			int pageSizeInt = Integer.parseInt(pageSize);
			int pageNumberInt = Integer.parseInt(pageNumber);
			PageHelper.startPage(pageNumberInt,pageSizeInt,true);//使用后数据库语句自动转为分页查询语句进行数据查询
		}
		if(Keyword==null || Keyword.trim().isEmpty())
			list = userDao.listAllStudents();

		else list = userDao.listAllStudentsByKeyword(Keyword);
	    
	    for(Map<String,Object> map : list){
	    	map.put("registerTime", sdf.format((Date)map.get("registerTime")));
	    }
	    System.out.println(JSONArray.fromObject(list).toString());
	    return list;
	}
	
	public List GetAllTeachers(String Keyword,String pageSize,String pageNumber){
		//分页所需相关参数的计算
		//根据参数查询学生成绩等字段，如果参数全部为空自动查询全部学生的相关成绩
		if(pageSize!=null&&pageNumber!=null) {
			int pageSizeInt = Integer.parseInt(pageSize);
			int pageNumberInt = Integer.parseInt(pageNumber);
			PageHelper.startPage(pageNumberInt,pageSizeInt,true);//使用后数据库语句自动转为分页查询语句进行数据查询
		}
        System.out.println("key=" + Keyword);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
		if(Keyword==null || Keyword.trim().isEmpty())
			list = userDao.listAllTeachers();

		else list = userDao.listAllTeachersByKeyword(Keyword);
	    
	    for(Map<String,Object> map : list){
	    	map.put("registerTime", sdf.format((Date)map.get("registerTime")));
	    }

	    return list;
	}

	//此处为AOP拦截Service记录异常信息。方法不需要加try-catch
	@SystemServiceLog(description = "添加一名学生用户")
	public int AddUser(User user){
		user.setState(new Integer(1));
		user.setRegistertime(new Date());
		return userDao.insertSelective(user);
	}

	@SystemServiceLog(description = "修改用户信息（用户管理）")
	public int UpdateUser(User user){
		return userDao.updateByPrimaryKeySelective(user);
	}
	
	public int DeleteUser(String id){
		return userDao.deleteByPrimaryKey(id);
	}
	

	
	public User getUser(String id){
		String emailRegex= "^[0-9A-Za-z][\\.-_0-9A-Za-z]*@[0-9A-Za-z]+(\\.[0-9A-Za-z]+)+$";

		if(Pattern.matches(emailRegex,id)) {	// 假如id为邮箱格式
			UserExample userExample = new UserExample();
			UserExample.Criteria criteria = userExample.createCriteria();
			criteria.andEmailEqualTo(id);
			User user = userDao.selectByExample(userExample).get(0);
			System.out.println("user==="+user.getUserId());
			System.out.println("user==="+user.getRealname());
			return user;
		}

		return userDao.selectByPrimaryKey(id);
	}
	
	public List<User> getAllTeacher()
	{
		UserExample userExample = new UserExample();
		UserExample.Criteria criteria = userExample.createCriteria();
		criteria.andLevelEqualTo(new Integer(1));
		return userDao.selectByExample(userExample);
	}
	
	public List<User> getTeacherById(String userId)
	{
		UserExample userExample = new UserExample();
		UserExample.Criteria criteria = userExample.createCriteria();
		criteria.andUserIdEqualTo(userId);
		return userDao.selectByExample(userExample);
	}

	@SystemServiceLog(description = "批量删除用户（用户管理）")
	public int DeleteAllUser(List<String> ids)
	{  
	   int count = 0;
	   for(String id : ids){
		 count += userDao.deleteByPrimaryKey(id);
		 //System.out.println("count=" + count);
	   } 
	   return count;
	} 
	
	public List<Map<String,Object>> selCourseNameByTeacherId(String teacherId,String keyword,String pageSize,String pageNumber) {
		List<Map<String,Object>> resultList = new ArrayList<Map<String,Object>>(); //返回结果的容器
		//分页所需相关参数的计算
		if(pageSize!=null&&pageNumber!=null) {
			int pageSizeInt = Integer.parseInt(pageSize);
			int pageNumberInt = Integer.parseInt(pageNumber);
			PageHelper.startPage(pageNumberInt,pageSizeInt,true);//使用后数据库语句自动转为分页查询语句进行数据查询
		}
		resultList = courseDao.selCourseNameByTeacherId(teacherId,keyword);
		return resultList;
	}
	
	public List<Map<String,Object>> selCourseObjById(String courseId) {
		
		return courseDao.selCourseObjById(courseId);
	}
	
	public int addCourse(String courseName) {
		//根据name查是否存在
		List<Map<String,Object>> courseList = courseDao.selCourseByName(courseName);
		if(courseList.isEmpty()) {
			//插入操作
			if(courseDao.insCourseByName(courseName) > 0) {
				return 1;
			}
			return 0;
		}else {
			return 2; 
		}
	}

	@SystemServiceLog(description = "添加课程任课关系")
	public int addTeach(String courseId,String teacherId) {
		String[] classIdArr = courseId.split(",");
		String[] teacherIdArr = teacherId.split(",");
		int insertNum = 0;
		if(classIdArr.length > 0 && teacherIdArr.length > 0) {
			for(String claStr : classIdArr) {
				courseId = claStr;
				for(String teaStr : teacherIdArr) {
					teacherId = teaStr;
					//校验是否已存在任课关系，有则跳过
					List<Map<String,Object>> confList = courseDao.selCourseConfByKeyword(courseId, teacherId);
					if(!confList.isEmpty()) {
						continue;
					}
					//进行插入操作
					courseDao.insertTeachByKeyword(courseId,teacherId);
					insertNum ++;
				}	
			}
			return insertNum;
		}
		return 0;
	}

	@SystemServiceLog(description = "删除课程")
	public int deleteCourse(String courseId) {
		int courseIdInt = Integer.parseInt(courseId);
		return courseDao.delCourseById(courseIdInt);
	}

	@SystemServiceLog(description = "批量删除课程")
	public int deleteAllCourse(List<String> ids) {
		 int count = 0;
		 if(!ids.isEmpty()) {
			 for(String id : ids){
				   count += courseDao.delCourseById(Integer.parseInt(id));
			   } 
		 }
		 return count;
	}

	@SystemServiceLog(description = "解除课程任课关系")
	public int deleteTeach(String courseId,String teacherId){
		int courseInt = Integer.parseInt(courseId);
		return courseDao.deleteTeachById(courseInt,teacherId);
	}

	@SystemServiceLog(description = "更新课程信息")
	public int updateCourse(int courseId,String courseName) {
		
		return courseDao.updateCourseById(courseId,courseName);
	}
	
}
