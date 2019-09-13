import tkinter as tk
from tkinter import ttk
#from tkinter import scrolledtext
import requests
from io import StringIO
import pandas as pd
import numpy as np
import threading
from ui import InitUI, Animation
from messageshow import ShowMessageBox

class Application(InitUI):
	def __init__(self, mainWin):
		super().__init__(mainWin)
		### set UI adaptor ###
		self.ui_adaptor = self.getRegAdaptor()
		
		### set search button function ###
		self.ui_btn = self.getSrchButton()
		self.ui_btn["command"] = self.click_start
		
		### create message box ###
		self.show_msg = ShowMessageBox()
		
		### create waitting animation and start thread ###
		#self.load_anim = LoadingAnimation(mainWin, 'loading_wait.gif')
		self.ui_animation_label = self.getAnimationLabel()
		self.animation = Animation(label=self.ui_animation_label, row=self.ui_adaptor.getUIInfo("aniLabel").getRowLoc(), column=self.ui_adaptor.getUIInfo("aniLabel").getColumnLoc(), filename='load3.gif')
		try:
			# 定義線程
			anim_thread = threading.Thread(target=self.animation.stop_play_animation)
			# 讓線程開始工作
			anim_thread.start()
		except:
			print("Error: unable to start anim_thread")
	
	def one_stock_statement(self, date_str):
		# 下載股價
		print(date_str)
		if date_str == "":
			print("please input stock name!")

		try:
			r = requests.post('http://www.twse.com.tw/exchangeReport/MI_INDEX?response=csv&date=' + self.getSrchDateInfo() + '&type=ALL')
		# 發生不合格憑證
		except requests.exceptions.SSLError:
			r = requests.post('http://www.twse.com.tw/exchangeReport/MI_INDEX?response=csv&date=' + self.getSrchDateInfo() + '&type=ALL', verify=False)
		except:
			# request web finish, stop waiting animation
			self.animation.stop_play_animation()
			self.show_msg.capture_exception()
			self.show_msg.show_mesg_box("Warning", "Query Web Time Out, Please Try Again!!")

		# 檢查狀態碼是否 OK
		if r.status_code == requests.codes.ok:
			print("OK")
		else:
			print("Request data wrong!!")

		return r
			
	def financial_statement(self, year, season, type='營益分析彙總表'):
		if year >= 1000:
			year -= 1911

		if type == '綜合損益彙總表':
			url = 'http://mops.twse.com.tw/mops/web/ajax_t163sb04'
		elif type == '資產負債彙總表':
			url = 'http://mops.twse.com.tw/mops/web/ajax_t163sb05'
		elif type == '營益分析彙總表':
			url = 'http://mops.twse.com.tw/mops/web/ajax_t163sb06'
		else:
			print('type does not match')

		r = requests.post(url, {
			'encodeURIComponent':1,
			'step':1,
			'firstin':1,
			'off':1,
			'TYPEK':'sii',
			'year':str(year),
			'season':str(season),
		})

		r.encoding = 'utf8'
		
		return r
		
	def classify_search_item(self, search_item, date_str, year, season):
		# 設定字符顯示寬度
		#pd.set_option('display.max_colwidth',500)
		pd.set_option('display.width', 500)
		pd.set_option('display.max_column', None)
		# 設定顯示最大行數
		pd.set_option('display.max_rows', None)
		# 設定中文列、行對齊
		pd.set_option('display.unicode.ambiguous_as_wide', True)
		pd.set_option('display.unicode.east_asian_width', True)

		if search_item == "股票交易資訊":
			rslt = self.one_stock_statement(date_str)
			# 整理資料，變成表格
			try:
				df = pd.read_csv(StringIO("\n".join([i.translate({ord(c): None for c in ' '})
													for i in rslt.text.split('\n')
													if len(i.split('",')) == 17 and i[0] != '='])), header=0)
				#df = df.drop(['Unnamed: 16'], axis=1)
				df = df.dropna(axis=1, how='any')
			except pd.errors.EmptyDataError:
				# request web finish, stop waiting animation
				self.animation.stop_play_animation()
				self.show_msg.show_mesg_box("Warning", "It Is Not A Work Day!!")
				return
		else:
			rslt = self.financial_statement(year, season, search_item)
			dfs = pd.read_html(rslt.text)
			
			if search_item == "綜合損益彙總表":
				for i, df in enumerate(dfs):
					if i == 3:
						df.columns = df.iloc[0]
						dfs[i] = df.iloc[1:]
			elif search_item == "資產負債彙總表":
				for i, df in enumerate(dfs):
					if i == 3:
						df.columns = df.iloc[0]
						dfs[i] = df.iloc[1:]
			else:
				for i, df in enumerate(dfs):
					df.columns = df.iloc[0]
					dfs[i] = df.iloc[1:]

			df = pd.concat(dfs).applymap(lambda x: x if x != '--' else np.nan)
			#print(df)
			#df = df.drop(['合計：共 889 家'], axis=1)
			df = df[df['公司代號'] != '公司代號']
			#print(df)
			df = df[~df['公司代號'].isnull()]
			df = df.dropna(axis=1, how='any')
			
		return df
			
	def classify_df(self, df, item, mode):
		print("classify_df" + " " + item)
		if item == "股票交易資訊":
			if mode == "單一股票資訊":
				print("classify_df" + mode)
				if self.getStockName() == "":
					# request web finish, stop waiting animation
					self.animation.stop_play_animation()
					self.show_msg.show_mesg_box("Error", "Please Enter Stock Name or Stock Number!!")
				else:
					try:
						index = list(df['證券名稱']).index(self.getStockName())
						return df.loc[index:index]
					except:
						index = list(df['證券代號']).index(self.getStockName())
						return df.loc[index:index]
			elif mode == "所有股票資訊":
				print("classify_df" + " " + mode)
				#df = df[(pd.to_numeric(df['本益比'], errors='coerce') < 15) & (pd.to_numeric(df['本益比'], errors='coerce') > 0)]
				return df
			elif mode == "股票篩選器":
				print(mode)
		elif item == "營益分析彙總表":
			print(item)
			df.index = range(len(df))
			if mode == "單一股票資訊":
				print("classify_df" + " " + mode)
				if self.getStockName() == "":
					# request web finish, stop waiting animation
					self.animation.stop_play_animation()
					self.show_msg.show_mesg_box("Error", "Please Enter Stock Name or Stock Number!!")
				else:
					try:
						index = list(df['公司名稱']).index(self.getStockName())
						#print(df)
						return df.loc[(index):(index)]
					except:
						index = list(df['公司代號']).index(self.getStockName())
						return df.loc[index:index]
			elif mode == "所有股票資訊":
				print("classify_df" + " " + mode)
				#df = df[df['公司代號'] != '公司代號']
				return df
			elif mode == "股票篩選器":
				print("classify_df" + " " + mode)
			return df
		elif item == "資產負債彙總表":
			print(item)
			df.index = range(len(df))
			if mode == "單一股票資訊":
				print("classify_df" + " " + mode)
				if self.getStockName() == "":
					# request web finish, stop waiting animation
					self.animation.stop_play_animation()
					self.show_msg.show_mesg_box("Error", "Please Enter Stock Name or Stock Number!!")
				else:
					try:
						index = list(df['公司名稱']).index(self.getStockName())
						#print(df)
						return df.loc[(index):(index)]
					except:
						index = list(df['公司代號']).index(self.getStockName())
						return df.loc[index:index]
			elif mode == "所有股票資訊":
				print("classify_df" + " " + mode)
				#df = df[df['公司代號'] != '公司代號']
				return df
			elif mode == "股票篩選器":
				print("classify_df" + " " + mode)
			return df
		elif item == "綜合損益彙總表":
			print(item)
			if mode == "單一股票資訊":
				print("classify_df" + " " + mode)
				df.index = range(len(df))
				if self.getStockName() == "":
					# request web finish, stop waiting animation
					self.animation.stop_play_animation()
					self.show_msg.show_mesg_box("Error", "Please Enter Stock Name or Stock Number!!")
				else:
					try:
						index = list(df['公司名稱']).index(self.getStockName())
						#print(df)
						return df.loc[(index):(index)]
					except:
						index = list(df['公司代號']).index(self.getStockName())
						return df.loc[index:index]
			elif mode == "所有股票資訊":
				print("classify_df" + " " + mode)
				#df = df[df['公司代號'] != '公司代號']
				return df
			elif mode == "股票篩選器":
				print("classify_df" + " " + mode)
			
	def query_stock(self):
		# 下載股價
		#print(self.getSrchDateInfo())
		#if self.getSrchDateInfo() == "":
		#	print("please input stock name!")
		self.srch_item = self.getSrchItemInfo()
		self.srch_mode = self.getSrchModeInfo()
		self.srch_date = self.getSrchDateInfo()
		self.srch_season = self.getSrchSeasonInfo()
		self.srch_year = self.getSrchYearInfo()
		
		# wait for request web
		self.animation.start_play_animation()
		
		# classify search item
		raw_srch_result = self.classify_search_item(self.srch_item, self.srch_date, self.srch_year, self.srch_season)
		#print(raw_srch_result)
		srch_result = self.classify_df(raw_srch_result, self.srch_item, self.srch_mode)

		# request web finish, stop waiting animation
		self.animation.stop_play_animation()

		# 設定text可以插入文字
		self.ui_rslt_text = self.getRsltWinText()
		self.ui_rslt_text["state"] = "normal"
		# 清空畫面
		self.ui_rslt_text.delete(1.0, tk.END)
		# 更新畫面
		self.ui_rslt_text.insert(1.0, srch_result)
		self.ui_rslt_text.grid(row=self.ui_adaptor.getUIInfo("rltText").getRowLoc(), column=self.ui_adaptor.getUIInfo("rltText").getColumnLoc(), sticky=tk.N+tk.W)
		self.ui_rslt_text["state"] = "disable"

		print("query_sock finish!!")

	def click_start(self):
		try:
			# 定義線程
			thread = threading.Thread(target=self.query_stock)
			# 讓線程開始工作
			thread.start()
		except:
			print("Error: unable to start thread")