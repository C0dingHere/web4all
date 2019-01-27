package com.fh.controller.system.dictionaries;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.fh.controller.base.BaseController;
import com.fh.entity.Page;
import com.fh.service.system.dictionaries.DictionariesService;
import com.fh.service.system.menu.MenuService;
import com.fh.util.AppUtil;
import com.fh.util.PageData;
/** 
 * 类名称：DictionariesController
 * 创建人：FH
 * 创建时间：2014年9月2日
 * @version
 */
@Controller
@RequestMapping(value="/dictionaries")
public class DictionariesController extends BaseController {
	
	@Resource(name="menuService")
	private MenuService menuService;
	@Resource(name="dictionariesService")
	private DictionariesService dictionariesService;
	

	
	/**
	 * 保存
	 */
	@RequestMapping(value="/save")
	public ModelAndView save(PrintWriter out) throws Exception{
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		PageData pdp = new PageData();
		pdp = this.getPageData();
		
		String PARENT_ID = pd.getString("PARENT_ID");
		pdp.put("ZD_ID", PARENT_ID);
		
		if(null == pd.getString("ZD_ID") || "".equals(pd.getString("ZD_ID"))){
			if(null != PARENT_ID && "0".equals(PARENT_ID)){
				pd.put("JB", 1);
				pd.put("P_BM", pd.getString("BIANMA"));
			}else{
				pdp = dictionariesService.findById(pdp);
				pd.put("JB", Integer.parseInt(pdp.get("JB").toString()) + 1);
				pd.put("P_BM", pdp.getString("BIANMA") + "_" + pd.getString("BIANMA"));
			}
			pd.put("ZD_ID", this.get32UUID());	//ID
			dictionariesService.save(pd);
		}else{
			pdp = dictionariesService.findById(pdp);
			if(null != PARENT_ID && "0".equals(PARENT_ID)){
				pd.put("P_BM",  pd.getString("BIANMA"));
			}else{
				pd.put("P_BM", pdp.getString("BIANMA") + "_" + pd.getString("BIANMA"));
			}
			
			dictionariesService.edit(pd);
		}
		
		mv.addObject("msg","success");
		mv.setViewName("save_result");
		return mv;
		
	}

	
	/**
	 * 列表
	 */
	List<PageData> szdList;
	@RequestMapping
	public ModelAndView list(Page page)throws Exception{
		
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		String PARENT_ID = pd.getString("PARENT_ID");
		
		
		if(null != PARENT_ID && !"".equals(PARENT_ID) && !"0".equals(PARENT_ID)){
			
			//返回按钮用
			PageData pdp = new PageData();
			pdp = this.getPageData();
			
			pdp.put("ZD_ID", PARENT_ID);
			pdp = dictionariesService.findById(pdp);
			mv.addObject("pdp", pdp);
			
			//头部导航
			szdList = new ArrayList<PageData>();
			this.getZDname(PARENT_ID);	//	逆序
			Collections.reverse(szdList);
			
		}
		
		
		String NAME = pd.getString("NAME");
		if(null != NAME && !"".equals(NAME)){
			NAME = NAME.trim();
			pd.put("NAME", NAME);
		}
		page.setShowCount(5);	//设置每页显示条数
		page.setPd(pd);				
		List<PageData> varList = dictionariesService.dictlistPage(page);
		
		mv.setViewName("system/dictionaries/zd_list");
		mv.addObject("varList", varList);
		mv.addObject("varsList", szdList);
		mv.addObject("pd", pd);
		
		return mv;
	}
	
	//递归
	public void getZDname(String PARENT_ID) {
		logBefore(logger, "递归");
		try {
			PageData pdps = new PageData();;
			pdps.put("ZD_ID", PARENT_ID);
			pdps = dictionariesService.findById(pdps);
			if(pdps != null){
				szdList.add(pdps);
				String PARENT_IDs = pdps.getString("PARENT_ID");
				this.getZDname(PARENT_IDs);
			}
		} catch (Exception e) {
			logger.error(e.toString(),e);
		}
	}
	
	/**
	 * 去新增页面
	 */
	@RequestMapping(value="/toAdd")
	public ModelAndView toAdd(Page page){
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		try{
			pd = this.getPageData();
			mv.setViewName("system/dictionaries/zd_edit");
			mv.addObject("pd", pd);
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		
		return mv;
	}
	
	
	
	/**
	 * 去编辑页面
	 */
	@RequestMapping(value="/toEdit")
	public ModelAndView toEdit( String ROLE_ID )throws Exception{
		ModelAndView mv = this.getModelAndView();
		PageData pd = new PageData();
		pd = this.getPageData();
		pd = dictionariesService.findById(pd);
		if(Integer.parseInt(dictionariesService.findCount(pd).get("ZS").toString()) != 0){
			mv.addObject("msg", "no");
		}else{
			mv.addObject("msg", "ok");
		}
		mv.setViewName("system/dictionaries/zd_edit");
		mv.addObject("pd", pd);
		return mv;
	}
	
	
	/**
	 * 判断编码是否存在
	 */
	@RequestMapping(value="/has")
	public void has(PrintWriter out){
		PageData pd = new PageData();
		try{
			pd = this.getPageData();
			if(dictionariesService.findBmCount(pd) != null){
				out.write("error");
			}else{
				out.write("success");
			}
			out.close();
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		
	}
	
	/**
	 * 删除用户
	 */
	@RequestMapping(value="/del")
	@ResponseBody
	public Object del(){
		Map<String,String> map = new HashMap<String,String>();
		PageData pd = new PageData();
		String errInfo = "";
		try{
			pd = this.getPageData();
			
			if(Integer.parseInt(dictionariesService.findCount(pd).get("ZS").toString()) != 0){
				errInfo = "false";
			}else{
				dictionariesService.delete(pd);
				errInfo = "success";
			}
		} catch(Exception e){
			logger.error(e.toString(), e);
		}
		
		map.put("result", errInfo);
		return AppUtil.returnObject(new PageData(), map);
		
	}
	

}
