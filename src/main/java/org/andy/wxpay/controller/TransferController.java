package org.andy.wxpay.controller;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.andy.wxpay.model.JsonResult;
import org.andy.wxpay.model.ResponseData;
import org.andy.wxpay.utils.CollectionUtil;
import org.andy.wxpay.utils.ConfigUtil;
import org.andy.wxpay.utils.HttpUtils;
import org.andy.wxpay.utils.PayUtil;
import org.andy.wxpay.utils.SerializerFeatureUtil;
import org.andy.wxpay.utils.StringUtil;
import org.andy.wxpay.utils.WebUtil;
import org.andy.wxpay.utils.XmlUtil;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.alibaba.fastjson.JSON;

/**
 * 创建时间：2016年11月9日 下午5:49:00
 * 
 * @author andy
 * @version 2.2
 */

@Controller
@RequestMapping("/transfer")
public class TransferController {

	private static final Logger LOG = Logger.getLogger(TransferController.class);
	
	private static final String TRANSFERS_PAY = "https://api.mch.weixin.qq.com/mmpaymkttransfers/promotion/transfers"; // 企业付款

	private static final String TRANSFERS_PAY_QUERY = "https://api.mch.weixin.qq.com/mmpaymkttransfers/gettransferinfo"; // 企业付款查询

	private static final String APP_ID = ConfigUtil.getProperty("wx.appid");

	private static final String MCH_ID = ConfigUtil.getProperty("wx.mchid");

	private static final String API_SECRET = ConfigUtil.getProperty("wx.api.secret");

	/**
	 * 企业向个人支付转账
	 * @param request
	 * @param response
	 * @param openid 用户openid
	 * @param callback
	 */
	@RequestMapping(value = "/pay", method = RequestMethod.POST)
	public void transferPay(HttpServletRequest request, HttpServletResponse response, String openid, String callback) {
		LOG.info("[/transfer/pay]");
		//业务判断 openid是否有收款资格
		
		Map<String, String> restmap = null;
		try {
			Map<String, String> parm = new HashMap<String, String>();
			parm.put("mch_appid", APP_ID); //公众账号appid
			parm.put("mchid", MCH_ID); //商户号
			parm.put("nonce_str", PayUtil.getNonceStr()); //随机字符串
			parm.put("partner_trade_no", PayUtil.getTransferNo()); //商户订单号
			parm.put("openid", openid); //用户openid	
			parm.put("check_name", "NO_CHECK"); //校验用户姓名选项 OPTION_CHECK
			//parm.put("re_user_name", "安迪"); //check_name设置为FORCE_CHECK或OPTION_CHECK，则必填
			parm.put("amount", "100"); //转账金额
			parm.put("desc", "测试转账到个人"); //企业付款描述信息
			parm.put("spbill_create_ip", PayUtil.getLocalIp(request)); //Ip地址
			parm.put("sign", PayUtil.getSign(parm, API_SECRET));

			String restxml = HttpUtils.posts(TRANSFERS_PAY, XmlUtil.xmlFormat(parm, false));
			restmap = XmlUtil.xmlParse(restxml);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		if (CollectionUtil.isNotEmpty(restmap) && "SUCCESS".equals(restmap.get("result_code"))) {
			LOG.info("转账成功：" + restmap.get("err_code") + ":" + restmap.get("err_code_des"));
			Map<String, String> transferMap = new HashMap<>();
			transferMap.put("partner_trade_no", restmap.get("partner_trade_no"));//商户转账订单号
			transferMap.put("payment_no", restmap.get("payment_no")); //微信订单号
			transferMap.put("payment_time", restmap.get("payment_time")); //微信支付成功时间
			WebUtil.response(response,
					WebUtil.packJsonp(callback,
							JSON.toJSONString(new JsonResult(1, "转账成功", new ResponseData(null, transferMap)),
									SerializerFeatureUtil.FEATURES)));
		}else {
			if (CollectionUtil.isNotEmpty(restmap)) {
				LOG.info("转账失败：" + restmap.get("err_code") + ":" + restmap.get("err_code_des"));
			}
			WebUtil.response(response, WebUtil.packJsonp(callback, JSON
					.toJSONString(new JsonResult(-1, "转账失败", new ResponseData()), SerializerFeatureUtil.FEATURES)));
		}
	}

	/**
	 * 企业向个人转账查询
	 * @param request
	 * @param response
	 * @param tradeno 商户转账订单号
	 * @param callback
	 */
	@RequestMapping(value = "/pay/query", method = RequestMethod.POST)
	public void orderPayQuery(HttpServletRequest request, HttpServletResponse response, String tradeno,
			String callback) {
		LOG.info("[/transfer/pay/query]");
		if (StringUtil.isEmpty(tradeno)) {
			WebUtil.response(response, WebUtil.packJsonp(callback, JSON
					.toJSONString(new JsonResult(-1, "转账订单号不能为空", new ResponseData()), SerializerFeatureUtil.FEATURES)));
		}

		Map<String, String> restmap = null;
		try {
			Map<String, String> parm = new HashMap<String, String>();
			parm.put("appid", APP_ID);
			parm.put("mch_id", MCH_ID);
			parm.put("partner_trade_no", tradeno);
			parm.put("nonce_str", PayUtil.getNonceStr());
			parm.put("sign", PayUtil.getSign(parm, API_SECRET));

			String restxml = HttpUtils.posts(TRANSFERS_PAY_QUERY, XmlUtil.xmlFormat(parm, true));
			restmap = XmlUtil.xmlParse(restxml);
		} catch (Exception e) {
			LOG.error(e.getMessage(), e);
		}

		if (CollectionUtil.isNotEmpty(restmap) && "SUCCESS".equals(restmap.get("result_code"))) {
			// 订单查询成功 处理业务逻辑
			LOG.info("订单查询：订单" + restmap.get("partner_trade_no") + "支付成功");
			Map<String, String> transferMap = new HashMap<>();
			transferMap.put("partner_trade_no", restmap.get("partner_trade_no"));//商户转账订单号
			transferMap.put("openid", restmap.get("openid")); //收款微信号
			transferMap.put("payment_amount", restmap.get("payment_amount")); //转账金额
			transferMap.put("transfer_time", restmap.get("transfer_time")); //转账时间
			transferMap.put("desc", restmap.get("desc")); //转账描述
			WebUtil.response(response, WebUtil.packJsonp(callback, JSON
					.toJSONString(new JsonResult(1, "订单转账成功", new ResponseData(null, transferMap)), SerializerFeatureUtil.FEATURES)));
		}else {
			if (CollectionUtil.isNotEmpty(restmap)) {
				LOG.info("订单转账失败：" + restmap.get("err_code") + ":" + restmap.get("err_code_des"));
			}
			WebUtil.response(response, WebUtil.packJsonp(callback, JSON
					.toJSONString(new JsonResult(-1, "订单转账失败", new ResponseData()), SerializerFeatureUtil.FEATURES)));
		}
	}

}
