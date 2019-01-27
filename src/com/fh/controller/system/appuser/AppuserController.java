package com.fh.controller.system.appuser;

import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fh.controller.base.BaseController;
import com.fh.entity.Page;
import com.fh.entity.system.Role;
import com.fh.service.system.appuser.AppuserService;
import com.fh.service.system.role.RoleService;
import com.fh.util.AppUtil;
import com.fh.util.Const;
import com.fh.util.Jurisdiction;
import com.fh.util.MD5;
import com.fh.util.ObjectExcelView;
import com.fh.util.PageData;

/** 
 * 类名称：AppuserController
 * 创建人：FH 
 * 创建时间：2014年6月28日
 * @version
 */
@Controller
@RequestMapping(value="/happuser")
public class AppuserController extends BaseController {
	
	String menuUrl = "happuser/listUsers.do"; //菜单地址(权限用)
	@Resource(name="appuserService")
	private AppuserService appuserService;
	@Resource(name="roleService")
	private RoleService roleService;
	
	
	
	/**
	 * 保存用户
	 */
	@RequestMapping(value="/saveU")
	public ModelAndView saveU(PrintWriter out) throws Exception{
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		
		pd.put("USER_ID", this.get32UUID());	//ID
		pd.put("RIGHTS", "");					//权限
		pd.put("LAST_LOGIN", "");				//最后登录时间
		pd.put("IP", "");						//IP
		//pd.put("STATUS", "0");				//状态
		
		pd.put("PASSWORD", MD5.md5(pd.getString("PASSWORD")));
		
		if(null == appuserService.findByUId(pd)){
			if(Jurisdiction.buttonJurisdiction(menuUrl, "add")){appuserService.saveU(pd);} //判断新增权限
			mv.addObject("msg","success");
		}else{
			mv.addObject("msg","failed");
		}
		mv.setViewName("save_result");
		return mv;
	}
	
	/**
	 * 修改用户
	 */
	@RequestMapping(value="/editU")
	public ModelAndView editU(PrintWriter out) throws Exception{
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		if(pd.getString("PASSWORD") != null && !"".equals(pd.getString("PASSWORD"))){
			pd.put("PASSWORD", MD5.md5(pd.getString("PASSWORD")));
		}
		if(Jurisdiction.buttonJurisdiction(menuUrl, "edit")){appuserService.editU(pd);}
		mv.addObject("msg","success");
		mv.setViewName("save_result");
		return mv;
	}
	
	/**
	 * 判断用户名是否存在
	 */
	@RequestMapping(value="/hasU")
	@ResponseBody
	public Object hasU(){
		Map<String,String> map = new HashMap<String,String>();
		String errInfo = "success";
		PageData pd = new PageData();
		try{
			pd = this.getPageData();
			if(appuserService.findByUId(pd) != null){
				errInfo = "error";
			}
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		map.put("result", errInfo);				//返回结果
		return AppUtil.returnObject(new PageData(), map);
	}
	
	/**
	 * 判断邮箱是否存在
	 */
	@RequestMapping(value="/hasE")
	@ResponseBody
	public Object hasE(){
		Map<String,String> map = new HashMap<String,String>();
		String errInfo = "success";
		PageData pd = new PageData();
		try{
			pd = this.getPageData();
			if(appuserService.findByUE(pd) != null){
				errInfo = "error";
			}
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		map.put("result", errInfo);				//返回结果
		return AppUtil.returnObject(new PageData(), map);
	}
	
	/**
	 * 判断编码是否存在
	 */
	@RequestMapping(value="/hasN")
	@ResponseBody
	public Object hasN(){
		Map<String,String> map = new HashMap<String,String>();
		String errInfo = "success";
		PageData pd = new PageData();
		try{
			pd = this.getPageData();
			if(appuserService.findByUN(pd) != null){
				errInfo = "error";
			}
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		map.put("result", errInfo);				//返回结果
		return AppUtil.returnObject(new PageData(), map);
	}
	
	/**
	 * 去修改用户页面
	 */
	@RequestMapping(value="/goEditU")
	public ModelAndView goEditU(){
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		try {
			List<Role> roleList = roleService.listAllappERRoles();			//列出所有二级角色
			pd = appuserService.findByUiId(pd);								//根据ID读取
			mv.setViewName("system/appuser/appuser_edit");
			mv.addObject("msg", "editU");
			mv.addObject("pd", pd);
			mv.addObject("roleList", roleList);
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}						
		return mv;
	}
	
	/**
	 * 去新增用户页面
	 */
	@RequestMapping(value="/goAddU")
	public ModelAndView goAddU(){
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		try {
			List<Role> roleList;
			roleList = roleService.listAllappERRoles();			//列出所有二级角色
			mv.setViewName("system/appuser/appuser_edit");
			mv.addObject("msg", "saveU");
			mv.addObject("pd", pd);
			mv.addObject("roleList", roleList);
		} catch (Exception e) {
			logger.error(e.toString(), e);
		}						
		return mv;
	}
	
	/**
	 * 显示用户列表
	 */
	@RequestMapping(value="/listUsers")
	public ModelAndView listUsers(Page page){
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		try{
			pd = this.getPageData();
			
			String USERNAME = pd.getString("USERNAME");
			
			if(null != USERNAME && !"".equals(USERNAME)){
				USERNAME = USERNAME.trim();
				pd.put("USERNAME", USERNAME);
			}
			
			page.setPd(pd);
			List<PageData>	userList = appuserService.listPdPageUser(page);			//列出用户列表
			List<Role> roleList = roleService.listAllappERRoles();					//列出所有会员二级角色
			
			mv.setViewName("system/appuser/appuser_list");
			mv.addObject("userList", userList);
			mv.addObject("roleList", roleList);
			mv.addObject("pd", pd);
			mv.addObject(Const.SESSION_QX,this.getHC());	//按钮权限
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		
		return mv;
	}

	/**
	 * 删除用户
	 */
	@RequestMapping(value="/deleteU")
	public void deleteU(PrintWriter out){
		PageData pd = new PageData();
		try{
			pd = this.getPageData();
			if(Jurisdiction.buttonJurisdiction(menuUrl, "del")){appuserService.deleteU(pd);}
			out.write("success");
			out.close();
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		
	}
	
	/**
	 * 批量删除
	 */
	@RequestMapping(value="/deleteAllU")
	@ResponseBody
	public Object deleteAllU() {
		PageData pd = new PageData();
		Map<String,Object> map = new HashMap<String,Object>();
		try {
			pd = this.getPageData();
			List<PageData> pdList = new ArrayList<PageData>();
			String USER_IDS = pd.getString("USER_IDS");
			
			if(null != USER_IDS && !"".equals(USER_IDS)){
				String ArrayUSER_IDS[] = USER_IDS.split(",");
				if(Jurisdiction.buttonJurisdiction(menuUrl, "del")){appuserService.deleteAllU(ArrayUSER_IDS);}
				pd.put("msg", "ok");
			}else{
				pd.put("msg", "no");
			}
			
			pdList.add(pd);
			map.put("list", pdList);
		} catch (Exception e) {
			logger.error(e.toString(), e);
		} finally {
			logAfter(logger);
		}
		return AppUtil.returnObject(pd, map);
	}
	
	
	/*
	 * 导出会员信息到excel
	 * @return
	 */
	@RequestMapping(value="/excel")
	public ModelAndView exportExcel(){
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		try{
			if(Jurisdiction.buttonJurisdiction(menuUrl, "cha")){	
				//检索条件===
				String USERNAME = pd.getString("USERNAME");
				if(null != USERNAME && !"".equals(USERNAME)){
					USERNAME = USERNAME.trim();
					pd.put("USERNAME", USERNAME);
				}
				String lastLoginStart = pd.getString("lastLoginStart");
				String lastLoginEnd = pd.getString("lastLoginEnd");
				if(lastLoginStart != null && !"".equals(lastLoginStart)){
					lastLoginStart = lastLoginStart+" 00:00:00";
					pd.put("lastLoginStart", lastLoginStart);
				}
				if(lastLoginEnd != null && !"".equals(lastLoginEnd)){
					lastLoginEnd = lastLoginEnd+" 00:00:00";
					pd.put("lastLoginEnd", lastLoginEnd);
				} 
				//检索条件===
				
				Map<String,Object> dataMap = new HashMap<String,Object>();
				List<String> titles = new ArrayList<String>();
				
				titles.add("用户名"); 		//1
				titles.add("编号");  		//2
				titles.add("姓名");			//3
				titles.add("手机号");		//4
				titles.add("身份证号");		//5
				titles.add("等级");			//6
				titles.add("邮箱");			//7
				titles.add("最近登录");		//8
				titles.add("到期时间");		//9
				titles.add("上次登录IP");	//10
				
				dataMap.put("titles", titles);
				
				List<PageData> userList = appuserService.listAllUser(pd);
				List<PageData> varList = new ArrayList<PageData>();
				for(int i=0;i<userList.size();i++){
					PageData vpd = new PageData();
					vpd.put("var1", userList.get(i).getString("USERNAME"));		//1
					vpd.put("var2", userList.get(i).getString("NUMBER"));		//2
					vpd.put("var3", userList.get(i).getString("NAME"));			//3
					vpd.put("var4", userList.get(i).getString("PHONE"));		//4
					vpd.put("var5", userList.get(i).getString("SFID"));			//5
					vpd.put("var6", userList.get(i).getString("ROLE_NAME"));	//6
					vpd.put("var7", userList.get(i).getString("EMAIL"));		//7
					vpd.put("var8", userList.get(i).getString("LAST_LOGIN"));	//8
					vpd.put("var9", userList.get(i).getString("END_TIME"));		//9
					vpd.put("var10", userList.get(i).getString("IP"));			//10
					varList.add(vpd);
				}
				
				dataMap.put("varList", varList);
				
				ObjectExcelView erv = new ObjectExcelView();
				mv = new ModelAndView(erv,dataMap);
			}
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		return mv;
	}
	
	//===================================================================================================
	
	
	
	
	@InitBinder
	public void initBinder(WebDataBinder binder){
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		binder.registerCustomEditor(Date.class, new CustomDateEditor(format,true));
	}
	
	/* ===============================权限================================== */
	public Map<String, String> getHC(){
		Subject currentUser = SecurityUtils.getSubject();  //shiro管理的session
		Session session = currentUser.getSession();
		return (Map<String, String>)session.getAttribute(Const.SESSION_QX);
	}
	/* ===============================权限================================== */
}
