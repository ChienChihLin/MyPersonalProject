import tkinter as tk
from tkinter import ttk
from tkinter import messagebox as msg
from PIL import Image, ImageTk
from tkcalendar import Calendar, DateEntry

class UIObject():
	def __init__(self, name, rowLoc, columnLoc, rowSpan, columnSpan):
		self.mName = name
		self.mRowLoc = rowLoc
		self.mColumnLoc = columnLoc
		self.mRowSpan = rowSpan
		self.mColumnSpan = columnSpan
	
	def getName(self):
		return self.mName
		
	def getRowLoc(self):
		return self.mRowLoc
		
	def getColumnLoc(self):
		return self.mColumnLoc
		
	def getRowSpan(self):
		return self.mRowSpan
	
	def getColumnSpan(self):
		return self.mColumnSpan

class RegisterUIAdapter():
	def __init__(self):
		self.dic = dict()

	def registerUI(self, name, rowLoc, columnLoc, rowSpan, columnSpan):
		tmpObj = UIObject(name, rowLoc, columnLoc, rowSpan, columnSpan)
		self.dic[tmpObj.getName()] = tmpObj

	def getUIInfo(self, name):
		return self.dic.get(name,"none")

	def showAllUI(self):
		return self.dic.items()

class Animation():
	def __init__(self, label, row, column, filename):
		im = Image.open(filename)
		seq =  []
		self.show_label = label
		self.row_loc = row
		self.column_loc = column
		self.never_start = True
		try:
			while 1:
				im_resize = self.resize(200, 200, im)
				seq.append(im_resize.copy())
				im.seek(len(seq)) # skip to next frame
		except EOFError:
			pass # we're done

		try:
			self.delay = im.info['duration']
		except KeyError:
			self.delay = 100

		first = seq[0].convert('RGBA')
		self.frames = [ImageTk.PhotoImage(first)]

		#tk.Label.__init__(self, master, image=self.frames[0])
		self.show_label["image"] = self.frames[0]

		temp = seq[0]
		for image in seq[1:]:
			temp.paste(image)
			frame = temp.convert('RGBA')
			self.frames.append(ImageTk.PhotoImage(frame))

		self.idx = 0

	# not to use now, it need to be fixed
	def resize(self, w_box, h_box, pil_image):
		w, h = pil_image.size
		f1 = 1.0*w_box/w
		f2 = 1.0*h_box/h
		factor = min([f1, f2])
		width = int(w*factor)
		height = int(h*factor)
		return pil_image.resize((width, height), Image.ANTIALIAS)

	def play_animation(self):
		self.show_label.config(image=self.frames[self.idx])
		self.idx += 1
		if self.idx == len(self.frames):
			self.idx = 0
		self.cancel = self.show_label.after(self.delay, self.play_animation)

	def start_play_animation(self):
		self.cancel = self.show_label.after(self.delay, self.play_animation)
		self.show_label.grid(row=self.row_loc, column=self.column_loc)
		self.never_start = False

	def stop_play_animation(self):
		if self.never_start != True:
			#self.idx = 0
			self.show_label.after_cancel(self.cancel)
			self.show_label.grid_forget()

class InitUI(tk.Frame): #(tk.Frame,UiLocationInfo):
	def __init__(self, mainWin):
		tk.Grid.rowconfigure(mainWin, 0, weight=1)
		#tk.Grid.rowconfigure(mainWin, 1, weight=1)
		#tk.Grid.rowconfigure(mainWin, 2, weight=1)
		#tk.Grid.rowconfigure(mainWin, 3, weight=1)
		tk.Grid.columnconfigure(mainWin, 0, weight=1)
		#tk.Grid.columnconfigure(mainWin, 1, weight=1)
		#tk.Grid.columnconfigure(mainWin, 2, weight=1)
		#tk.Grid.columnconfigure(mainWin, 3, weight=1)
		tk.Frame.__init__(self, mainWin)
		#UiLocationInfo.__init__(self)
		# 設定主畫面
		mainWin.title("Stock Application")
		mainWin.geometry("1200x600+100+0")
		self.grid()
		# 設定option background color
		style = ttk.Style()
		style.configure("TMenubutton", background='#DDDDDD')
		# create ui adaptor
		self.mRegAdaptor = RegisterUIAdapter()
		# 建立所有視窗元件 
		self.createWidgets()
		
		### search option init ###
		#self.searchItemInfo = ""
		#self.searchModeInfo = ""
		#self.searchSeasonInfo = ""
		#self.searchDateInfo = ""
		self.searchModeInfo = self.getSrchDftModeInfo()
		self.searchItemInfo = self.getSrchDftItemInfo()
		self.searchYearInfo = self.getSrchDftYearInfo()
		self.searchSeasonInfo = self.getSrchDftSeasonInfo()
		self.searchDateInfo = self.getSrchDateInfo()
		self.searchItemDic = {
			"綜合損益彙總表":self.taskForPrftLoss,
			"資產負債彙總表":self.taskForBalncSht,
			"營益分析彙總表":self.taskForPrftAnlys,
			"股票交易資訊":self.taskForDayTrade,
		}
		self.searchModeDic = {
			"所有股票資訊":self.taskForAllStock,
			"單一股票資訊":self.taskForOneStock,
			"股票篩選器":self.taskForStockAnlys,
		}

	def createWidgets(self):
		# 設定查詢結果顯示畫面
		#self.listbox = tk.Listbox(self)
		#self.listbox["width"] = 60
		#self.listbox.grid(row=1, column=0, sticky=tk.N+tk.W)
		#self.text = scrolledtext.ScrolledText(self)
		self.text = tk.Text(self)
		self.text["width"] = 100
		self.text["height"] = 35
		self.text["wrap"] = "none"
		self.text["borderwidth"] = 0
		self.text["state"] = "disable"
		self.textVsb = tk.Scrollbar(self)
		self.textVsb["orient"] = "vertical"
		self.textVsb["command"] = self.text.yview
		self.textHsb = tk.Scrollbar(self)
		self.textHsb["orient"] = "horizontal"
		self.textHsb["command"] = self.text.xview
		self.text.configure(yscrollcommand=self.textVsb.set, xscrollcommand=self.textHsb.set)
		# set result text location
		self.mRegAdaptor.registerUI("rltText", 0, 0, 400, 600)
		self.text.grid(row=self.mRegAdaptor.getUIInfo("rltText").getRowLoc(), column=self.mRegAdaptor.getUIInfo("rltText").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("rltText").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("rltText").getRowSpan(), sticky=tk.N+tk.W)
		# set result text vertical bar location
		self.mRegAdaptor.registerUI("rltTextVsb", self.mRegAdaptor.getUIInfo("rltText").getRowLoc(), (self.mRegAdaptor.getUIInfo("rltText").getColumnLoc()+self.mRegAdaptor.getUIInfo("rltText").getColumnSpan()), self.mRegAdaptor.getUIInfo("rltText").getRowSpan(), 2)
		self.textVsb.grid(row=self.mRegAdaptor.getUIInfo("rltTextVsb").getRowLoc(), column=self.mRegAdaptor.getUIInfo("rltTextVsb").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("rltTextVsb").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("rltTextVsb").getRowSpan(), sticky=tk.S+tk.N)
		# set result text horizontal bar location
		self.mRegAdaptor.registerUI("rltTextHsb", (self.mRegAdaptor.getUIInfo("rltText").getRowLoc()+self.mRegAdaptor.getUIInfo("rltText").getRowSpan()), self.mRegAdaptor.getUIInfo("rltText").getColumnLoc(), 2, self.mRegAdaptor.getUIInfo("rltText").getColumnSpan())
		self.textHsb.grid(row=self.mRegAdaptor.getUIInfo("rltTextHsb").getRowLoc(), column=self.mRegAdaptor.getUIInfo("rltTextHsb").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("rltTextHsb").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("rltTextHsb").getRowSpan(), sticky=tk.E+tk.W) 
		
		# 設定dummy label
		self.dummyVsbBarLabel = tk.Label(self)
		self.dummyVsbBarLabel["width"] = 5
		#self.dummyVsbBarLabel["bg"] = "yellow"
		self.mRegAdaptor.registerUI("dummyVsbBar", self.mRegAdaptor.getUIInfo("rltTextVsb").getRowLoc(), (self.mRegAdaptor.getUIInfo("rltTextVsb").getColumnLoc()+self.mRegAdaptor.getUIInfo("rltTextVsb").getColumnSpan()), self.mRegAdaptor.getUIInfo("rltTextVsb").getRowSpan(), 2)
		self.dummyVsbBarLabel.grid(row=self.mRegAdaptor.getUIInfo("dummyVsbBar").getRowLoc(), column=self.mRegAdaptor.getUIInfo("dummyVsbBar").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("dummyVsbBar").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("dummyVsbBar").getRowSpan(), sticky=tk.S+tk.N)
		#self.dummyLabel.grid_forget()
		#print(self.mRegAdaptor.getUIInfo("dummyVsbBar").getRowLoc())
		#print(self.mRegAdaptor.getUIInfo("dummyVsbBar").getColumnLoc())
		
		# 設定查詢容器frame位置
		self.searchFrame = tk.Frame(self)
		#self.searchFrame["bg"] = "red"
		self.searchFrame["height"] = 260
		self.searchFrame["width"] = 150
		self.searchFrame.configure(heigh=self.searchFrame["height"],width=self.searchFrame["width"])
		self.searchFrame.grid_propagate(0)
		self.mRegAdaptor.registerUI("searchFrame", self.mRegAdaptor.getUIInfo("dummyVsbBar").getRowLoc(), (self.mRegAdaptor.getUIInfo("dummyVsbBar").getColumnLoc()+self.mRegAdaptor.getUIInfo("dummyVsbBar").getColumnSpan()), int(self.mRegAdaptor.getUIInfo("dummyVsbBar").getRowSpan()*2/4), 2)
		self.searchFrame.grid(row=self.mRegAdaptor.getUIInfo("searchFrame").getRowLoc(), column=self.mRegAdaptor.getUIInfo("searchFrame").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("searchFrame").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("searchFrame").getRowSpan(), sticky=tk.W+tk.N)
		
		# 設定查詢項目label位置
		self.searchLabel = ttk.Label(self.searchFrame)
		self.searchLabel["width"] = 12
		#self.searchLabel["justify"] = "left"
		self.searchLabel["text"] = "選擇查詢項目:"
		self.mRegAdaptor.registerUI("searchLabel", 0, 0, 2, 2)
		self.searchLabel.grid(row=self.mRegAdaptor.getUIInfo("searchLabel").getRowLoc(), column=self.mRegAdaptor.getUIInfo("searchLabel").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("searchLabel").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("searchLabel").getRowSpan(), sticky=tk.W+tk.N)
		
		# 設定查詢項目option位置
		self.searchOptionStr = tk.StringVar()
		self.searchOptionVar = ['股票交易資訊', '綜合損益彙總表','資產負債彙總表','營益分析彙總表']
		self.searchOptionStr.set(self.searchOptionVar[0])
		self.searchOptionMenu = ttk.OptionMenu(self.searchFrame, self.searchOptionStr, self.searchOptionVar[0], *self.searchOptionVar, command=self.search_item_func)
		self.searchOptionMenu["width"] = 12
		#self.searchOptionMenu["justify"] = "left"
		self.mRegAdaptor.registerUI("searchOption", (self.mRegAdaptor.getUIInfo("searchLabel").getRowLoc()+self.mRegAdaptor.getUIInfo("searchLabel").getRowSpan()), self.mRegAdaptor.getUIInfo("searchLabel").getColumnLoc(), 2, self.mRegAdaptor.getUIInfo("searchLabel").getColumnSpan())
		self.searchOptionMenu.grid(row=self.mRegAdaptor.getUIInfo("searchOption").getRowLoc(), column=self.mRegAdaptor.getUIInfo("searchOption").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("searchOption").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("searchOption").getRowSpan(),sticky=tk.W+tk.N)
		#self.searchOptionMenu.grid_forget()
		
		# 設定dummy label
		self.dummySrchOptLabel = tk.Label(self.searchFrame)
		self.dummySrchOptLabel["heigh"] = 2
		self.dummySrchOptLabel["width"] = 12
		#self.dummySrchOptLabel["justify"] = "left"
		self.mRegAdaptor.registerUI("dummySrchOpt", (self.mRegAdaptor.getUIInfo("searchOption").getRowLoc()+self.mRegAdaptor.getUIInfo("searchOption").getRowSpan()), self.mRegAdaptor.getUIInfo("searchOption").getColumnLoc(), 2, self.mRegAdaptor.getUIInfo("searchOption").getColumnSpan())
		self.dummySrchOptLabel.grid(row=self.mRegAdaptor.getUIInfo("dummySrchOpt").getRowLoc(), column=self.mRegAdaptor.getUIInfo("dummySrchOpt").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("dummySrchOpt").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("dummySrchOpt").getRowSpan(), sticky=tk.W+tk.N)
		#self.dummyLabel.grid_forget()
		
		# 設定查詢模式label位置
		self.searchModeLabel = ttk.Label(self.searchFrame)
		self.searchModeLabel["width"] = 12
		#self.searchModeLabel["justify"] = "left"
		self.searchModeLabel["text"] = "選擇查詢模式:"
		self.mRegAdaptor.registerUI("SearchModLabel", (self.mRegAdaptor.getUIInfo("dummySrchOpt").getRowLoc()+self.mRegAdaptor.getUIInfo("dummySrchOpt").getRowSpan()), self.mRegAdaptor.getUIInfo("dummySrchOpt").getColumnLoc(), 2, self.mRegAdaptor.getUIInfo("dummySrchOpt").getColumnSpan())
		self.searchModeLabel.grid(row=self.mRegAdaptor.getUIInfo("SearchModLabel").getRowLoc(), column=self.mRegAdaptor.getUIInfo("SearchModLabel").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("SearchModLabel").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("SearchModLabel").getRowSpan(), sticky=tk.W+tk.N)
		
		# 設定查詢模式option位置
		self.searchModeOptionStr = tk.StringVar()
		self.searchModeOptionVar = ['所有股票資訊','單一股票資訊','股票篩選器']
		self.searchModeOptionStr.set(self.searchModeOptionVar[0])
		self.searchModeOpionMenu = ttk.OptionMenu(self.searchFrame, self.searchModeOptionStr, self.searchModeOptionVar[0], *self.searchModeOptionVar, command=self.search_mode_func, style="TMenubutton")
		self.searchModeOpionMenu["width"] = 12
		#self.searchModeOpionMenu["justify"] = "left"
		self.mRegAdaptor.registerUI("SearchModOption", (self.mRegAdaptor.getUIInfo("SearchModLabel").getRowLoc()+self.mRegAdaptor.getUIInfo("SearchModLabel").getRowSpan()), self.mRegAdaptor.getUIInfo("SearchModLabel").getColumnLoc(), 2, self.mRegAdaptor.getUIInfo("SearchModLabel").getColumnSpan())
		self.searchModeOpionMenu.grid(row=self.mRegAdaptor.getUIInfo("SearchModOption").getRowLoc(), column=self.mRegAdaptor.getUIInfo("SearchModOption").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("SearchModOption").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("SearchModOption").getRowSpan() ,sticky=tk.W+tk.N)
		#self.searchModeOpionMenu.grid_forget()
		
		# 設定dummy label
		self.dummySrchModOptLabel = tk.Label(self.searchFrame)
		self.dummySrchModOptLabel["heigh"] = 2
		self.dummySrchOptLabel["width"] = 12
		#self.dummySrchModOptLabel["justify"] = "left"
		self.mRegAdaptor.registerUI("dummySrchModOpt", (self.mRegAdaptor.getUIInfo("SearchModOption").getRowLoc()+self.mRegAdaptor.getUIInfo("SearchModOption").getRowSpan()), self.mRegAdaptor.getUIInfo("SearchModOption").getColumnLoc(), 2, self.mRegAdaptor.getUIInfo("SearchModOption").getColumnSpan())
		self.dummySrchModOptLabel.grid(row=self.mRegAdaptor.getUIInfo("dummySrchModOpt").getRowLoc(), column=self.mRegAdaptor.getUIInfo("dummySrchModOpt").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("dummySrchModOpt").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("dummySrchModOpt").getRowSpan(),sticky=tk.W+tk.N)
		#self.dummyLabel.grid_forget()
		
		# 設定股票篩選器label位置
		self.stockFilterLabel = ttk.Label(self.searchFrame)
		self.stockFilterLabel["width"] = 12
		#self.searchDateLabel["justify"] = "left"
		self.stockFilterLabel["text"] = "選擇篩選內容:"
		self.mRegAdaptor.registerUI("StockFilterLabel", (self.mRegAdaptor.getUIInfo("dummySrchModOpt").getRowLoc()+self.mRegAdaptor.getUIInfo("dummySrchModOpt").getRowSpan()), self.mRegAdaptor.getUIInfo("dummySrchModOpt").getColumnLoc(), 2, self.mRegAdaptor.getUIInfo("dummySrchModOpt").getColumnSpan())
		self.stockFilterLabel.grid(row=self.mRegAdaptor.getUIInfo("StockFilterLabel").getRowLoc(), column=self.mRegAdaptor.getUIInfo("StockFilterLabel").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("StockFilterLabel").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("StockFilterLabel").getRowSpan(), sticky=tk.W+tk.N)
		self.stockFilterLabel.grid_forget()
		
		# 設定股票篩選器option位置
		self.stockFilterOptionStr = tk.StringVar()
		self.stockFilterOptionVar = ["本益比 < 10"]
		self.stockFilterOptionStr.set(self.stockFilterOptionVar[0])
		self.stockFilterOpionMenu = ttk.OptionMenu(self.searchFrame, self.stockFilterOptionStr, self.stockFilterOptionVar[0], *self.stockFilterOptionVar, command=self.stock_filter_func, style="TMenubutton")
		self.stockFilterOpionMenu["width"] = 12
		#self.searchModeOpionMenu["justify"] = "left"
		self.mRegAdaptor.registerUI("StockFilterOption", (self.mRegAdaptor.getUIInfo("StockFilterLabel").getRowLoc()+self.mRegAdaptor.getUIInfo("StockFilterLabel").getRowSpan()), self.mRegAdaptor.getUIInfo("StockFilterLabel").getColumnLoc(), 2, 2)
		self.stockFilterOpionMenu.grid(row=self.mRegAdaptor.getUIInfo("StockFilterOption").getRowLoc(), column=self.mRegAdaptor.getUIInfo("StockFilterOption").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("StockFilterOption").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("StockFilterOption").getRowSpan() ,sticky=tk.W+tk.N)
		self.stockFilterOpionMenu.grid_forget()
		
################################################################################
###
		# 綜合損益彙總表篩選器
		self.PrftLossFltOptStr = tk.StringVar()
		self.PrftLossFltOptVar = ["本益比 < 10"]
		self.PrftLossFltOptStr.set(self.PrftLossFltOptVar[0])
		self.PrftLossFltOptMenu = ttk.OptionMenu(self.searchFrame, self.PrftLossFltOptStr, self.PrftLossFltOptVar[0], *self.PrftLossFltOptVar, command=self.stock_filter_func, style="TMenubutton")
		self.PrftLossFltOptMenu["width"] = 12
		self.mRegAdaptor.registerUI("PrftLossFltOpt", (self.mRegAdaptor.getUIInfo("StockFilterLabel").getRowLoc()+self.mRegAdaptor.getUIInfo("StockFilterLabel").getRowSpan()), self.mRegAdaptor.getUIInfo("StockFilterLabel").getColumnLoc(), 2, 2)
		self.PrftLossFltOptMenu.grid(row=self.mRegAdaptor.getUIInfo("PrftLossFltOpt").getRowLoc(), column=self.mRegAdaptor.getUIInfo("PrftLossFltOpt").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("PrftLossFltOpt").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("PrftLossFltOpt").getRowSpan() ,sticky=tk.W+tk.N)
		self.PrftLossFltOptMenu.grid_forget()
		# 資產負債彙總表篩選器
		self.BalncShtFltOptStr = tk.StringVar()
		self.BalncShtFltOptVar = ["本益比 < 10"]
		self.BalncShtFltOptStr.set(self.BalncShtFltOptVar[0])
		self.BalncShtFltOptMenu = ttk.OptionMenu(self.searchFrame, self.BalncShtFltOptStr, self.BalncShtFltOptVar[0], *self.BalncShtFltOptVar, command=self.stock_filter_func, style="TMenubutton")
		self.BalncShtFltOptMenu["width"] = 12
		self.mRegAdaptor.registerUI("BalncShtFltOpt", (self.mRegAdaptor.getUIInfo("StockFilterLabel").getRowLoc()+self.mRegAdaptor.getUIInfo("StockFilterLabel").getRowSpan()), self.mRegAdaptor.getUIInfo("StockFilterLabel").getColumnLoc(), 2, 2)
		self.BalncShtFltOptMenu.grid(row=self.mRegAdaptor.getUIInfo("BalncShtFltOpt").getRowLoc(), column=self.mRegAdaptor.getUIInfo("BalncShtFltOpt").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("BalncShtFltOpt").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("BalncShtFltOpt").getRowSpan() ,sticky=tk.W+tk.N)
		self.BalncShtFltOptMenu.grid_forget()
		# 營益分析彙總表篩選器
		self.PrftAnlysFltOptStr = tk.StringVar()
		self.PrftAnlysFltOptVar = ["本益比 < 10"]
		self.PrftAnlysFltOptStr.set(self.PrftAnlysFltOptVar[0])
		self.PrftAnlysFltOptMenu = ttk.OptionMenu(self.searchFrame, self.PrftAnlysFltOptStr, self.PrftAnlysFltOptVar[0], *self.PrftAnlysFltOptVar, command=self.stock_filter_func, style="TMenubutton")
		self.PrftAnlysFltOptMenu["width"] = 12
		self.mRegAdaptor.registerUI("PrftAnlysFltOpt", (self.mRegAdaptor.getUIInfo("StockFilterLabel").getRowLoc()+self.mRegAdaptor.getUIInfo("StockFilterLabel").getRowSpan()), self.mRegAdaptor.getUIInfo("StockFilterLabel").getColumnLoc(), 2, 2)
		self.PrftAnlysFltOptMenu.grid(row=self.mRegAdaptor.getUIInfo("PrftAnlysFltOpt").getRowLoc(), column=self.mRegAdaptor.getUIInfo("PrftAnlysFltOpt").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("PrftAnlysFltOpt").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("PrftAnlysFltOpt").getRowSpan() ,sticky=tk.W+tk.N)
		self.PrftAnlysFltOptMenu.grid_forget()
		# 股票交易資訊篩選器
###
################################################################################
		
		# 設定查詢期間label位置
		self.searchDateLabel = ttk.Label(self.searchFrame)
		self.searchDateLabel["width"] = 12
		#self.searchDateLabel["justify"] = "left"
		self.searchDateLabel["text"] = "選擇查詢時間:"
		self.mRegAdaptor.registerUI("SearchDateLabel", (self.mRegAdaptor.getUIInfo("dummySrchModOpt").getRowLoc()+self.mRegAdaptor.getUIInfo("dummySrchModOpt").getRowSpan()), self.mRegAdaptor.getUIInfo("dummySrchModOpt").getColumnLoc(), 2, self.mRegAdaptor.getUIInfo("dummySrchModOpt").getColumnSpan())
		self.searchDateLabel.grid(row=self.mRegAdaptor.getUIInfo("SearchDateLabel").getRowLoc(), column=self.mRegAdaptor.getUIInfo("SearchDateLabel").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("SearchDateLabel").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("SearchDateLabel").getRowSpan(), sticky=tk.W+tk.N)
		#self.searchDateLabel.grid_forget()
		
		# 設定查詢時間Calender位置
		self.firstCal = DateEntry(self.searchFrame, background='darkblue', foreground='white', width=12)
		self.mRegAdaptor.registerUI("FirstDateCal", (self.mRegAdaptor.getUIInfo("SearchDateLabel").getRowLoc()+self.mRegAdaptor.getUIInfo("SearchDateLabel").getRowSpan()), self.mRegAdaptor.getUIInfo("SearchDateLabel").getColumnLoc(), 2, 2)
		self.firstCal.grid(row=self.mRegAdaptor.getUIInfo("FirstDateCal").getRowLoc(), column=self.mRegAdaptor.getUIInfo("FirstDateCal").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("FirstDateCal").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("FirstDateCal").getRowSpan(), sticky=tk.W+tk.N)
		#self.firstCal.grid_forget()
		self.firstCal.bind("<<DateEntrySelected>>", self.first_cal_sel)
		
		# 設定查詢年度label位置
		self.yearLabel = tk.Label(self.searchFrame)
		#self.yearLabel["width"] = 12
		#self.searchDateLabel["justify"] = "left"
		self.yearLabel["text"] = "年度:"
		self.mRegAdaptor.registerUI("yearLabel", (self.mRegAdaptor.getUIInfo("SearchDateLabel").getRowLoc()+self.mRegAdaptor.getUIInfo("SearchDateLabel").getRowSpan()), self.mRegAdaptor.getUIInfo("SearchDateLabel").getColumnLoc(), 2, int(self.mRegAdaptor.getUIInfo("SearchDateLabel").getColumnSpan()/2))
		#self.mRegAdaptor.registerUI("yearLabel", (self.mRegAdaptor.getUIInfo("SearchDateLabel").getRowLoc()+self.mRegAdaptor.getUIInfo("SearchDateLabel").getRowSpan()), self.mRegAdaptor.getUIInfo("SearchDateLabel").getColumnLoc(), 2, 2)
		#self.yearLabel.grid(row=self.mRegAdaptor.getUIInfo("yearLabel").getRowLoc(), column=self.mRegAdaptor.getUIInfo("yearLabel").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("yearLabel").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("yearLabel").getRowSpan(), sticky=tk.W+tk.N)
		
		# 設定查詢年度option位置
		self.srchYearOptionStr = tk.StringVar()
		self.srchYearOptionVar = []
		for i in range(80, 150):
			self.srchYearOptionVar.append(i)
		self.srchYearOptionStr.set(self.srchYearOptionVar[27])
		self.srchYearOpionMenu = ttk.OptionMenu(self.searchFrame, self.srchYearOptionStr, self.srchYearOptionVar[27], *self.srchYearOptionVar, command=self.search_year_func)
		#self.srchSeasonOpionMenu["width"] = 12
		#self.searchModeOpionMenu["justify"] = "left"
		self.mRegAdaptor.registerUI("srchYearOpion", self.mRegAdaptor.getUIInfo("yearLabel").getRowLoc(), (self.mRegAdaptor.getUIInfo("yearLabel").getColumnLoc()+self.mRegAdaptor.getUIInfo("yearLabel").getColumnSpan()), 2, int(self.mRegAdaptor.getUIInfo("SearchDateLabel").getColumnSpan()/2))
		#self.srchYearOpionMenu.grid(row=self.mRegAdaptor.getUIInfo("srchYearOpion").getRowLoc(), column=self.mRegAdaptor.getUIInfo("srchYearOpion").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("srchYearOpion").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("srchYearOpion").getRowSpan(), sticky=tk.W+tk.N)
		#self.srchYearOpionMenu.grid_forget()
		
		# 設定查詢季度label位置
		self.seasonLabel = tk.Label(self.searchFrame)
		#self.searchDateLabel["width"] = 12
		#self.searchDateLabel["justify"] = "left"
		self.seasonLabel["text"] = "季度:"
		self.mRegAdaptor.registerUI("seasonLabel", (self.mRegAdaptor.getUIInfo("yearLabel").getRowLoc()+self.mRegAdaptor.getUIInfo("yearLabel").getRowSpan()), self.mRegAdaptor.getUIInfo("yearLabel").getColumnLoc(), 2, self.mRegAdaptor.getUIInfo("yearLabel").getColumnSpan())
		#self.seasonLabel.grid(row=self.mRegAdaptor.getUIInfo("seasonLabel").getRowLoc(), column=self.mRegAdaptor.getUIInfo("seasonLabel").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("seasonLabel").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("seasonLabel").getRowSpan(), sticky=tk.W+tk.N)
		#self.searchDateLabel.grid_forget()
				
		# 設定查詢季度option位置
		self.srchSeasonOptionStr = tk.StringVar()
		self.srchSeasonOptionVar = []
		for i in range(1, 5):
			self.srchSeasonOptionVar.append(i)
		self.srchSeasonOptionStr.set(self.srchSeasonOptionVar[0])
		#self.srchSeasonOptionVar = ['第一季','第二季','第三季','第四季']
		#self.srchSeasonOptionStr.set(self.srchSeasonOptionVar[0])
		self.srchSeasonOpionMenu = ttk.OptionMenu(self.searchFrame, self.srchSeasonOptionStr, self.srchSeasonOptionVar[0], *self.srchSeasonOptionVar, command=self.search_season_func)
		#self.srchSeasonOpionMenu["width"] = 12
		#self.searchModeOpionMenu["justify"] = "left"
		self.mRegAdaptor.registerUI("srchSeasonOpion", self.mRegAdaptor.getUIInfo("seasonLabel").getRowLoc(), (self.mRegAdaptor.getUIInfo("seasonLabel").getColumnLoc()+self.mRegAdaptor.getUIInfo("seasonLabel").getColumnSpan()), 2, self.mRegAdaptor.getUIInfo("srchYearOpion").getColumnSpan())
		#self.srchSeasonOpionMenu.grid(row=self.mRegAdaptor.getUIInfo("srchSeasonOpion").getRowLoc(), column=self.mRegAdaptor.getUIInfo("srchSeasonOpion").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("srchSeasonOpion").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("srchSeasonOpion").getRowSpan(), sticky=tk.W+tk.N)
		#self.srchSeasonOpionMenu.grid_forget()
		
		# 設定收尋股票容器frame位置
		self.searchStock = tk.Frame(self)
		#self.searchStock["bg"] = "black"
		#self.searchFrame["heigh"] = 25
		self.searchStock["height"] = 100
		self.searchStock["width"] = 150
		self.searchStock.configure(height=self.searchStock["height"],width=self.searchStock["width"])
		self.searchStock.grid_propagate(0)
		self.mRegAdaptor.registerUI("searchStockFrame", (self.mRegAdaptor.getUIInfo("searchFrame").getRowLoc()+self.mRegAdaptor.getUIInfo("searchFrame").getRowSpan()), self.mRegAdaptor.getUIInfo("searchFrame").getColumnLoc(), int(self.mRegAdaptor.getUIInfo("dummyVsbBar").getRowSpan()*1/4), 2)
		self.searchStock.grid(row=self.mRegAdaptor.getUIInfo("searchStockFrame").getRowLoc(), column=self.mRegAdaptor.getUIInfo("searchStockFrame").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("searchStockFrame").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("searchStockFrame").getRowSpan(), sticky=tk.N+tk.W)
		#self.searchStock.grid_forget()
		
		if 0:
			# 設定dummy label
			self.dummySrchSonOptLabel = tk.Label(self.searchStock)
			#self.dummySrchSonOptLabel["heigh"] = 1
			self.dummySrchSonOptLabel["width"] = 12
			#self.dummySrchOptLabel["justify"] = "left"
			self.mRegAdaptor.registerUI("dummySrchSonOpt", 0, 0, 2, 2)
			self.dummySrchSonOptLabel.grid(row=self.mRegAdaptor.getUIInfo("dummySrchSonOpt").getRowLoc(), column=self.mRegAdaptor.getUIInfo("dummySrchSonOpt").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("dummySrchSonOpt").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("dummySrchSonOpt").getRowSpan(), sticky=tk.W+tk.N)
			
		# 設定查詢股票名稱
		self.stockNameLabel = ttk.Label(self.searchStock)
		self.stockNameLabel["text"] = "查詢股票名稱或代號:"
		self.stockNameLabel["width"] = 18
		#self.mRegAdaptor.registerUI("stockNameLabel", (self.mRegAdaptor.getUIInfo("dummySrchSonOpt").getRowLoc()+self.mRegAdaptor.getUIInfo("dummySrchSonOpt").getRowSpan()), self.mRegAdaptor.getUIInfo("dummySrchSonOpt").getColumnLoc(), 2, self.mRegAdaptor.getUIInfo("dummySrchSonOpt").getColumnSpan())
		self.mRegAdaptor.registerUI("stockNameLabel", 0, 0, 2, 2)
		self.stockNameLabel.grid(row=self.mRegAdaptor.getUIInfo("stockNameLabel").getRowLoc(), column=self.mRegAdaptor.getUIInfo("stockNameLabel").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("stockNameLabel").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("stockNameLabel").getRowSpan(), sticky=tk.W+tk.N)
		self.stockNameLabel.grid_forget()
		
		# 設定查詢股票名稱Entry
		self.stockNameStr = tk.StringVar()
		#self.stockNameStr.set("台積電")
		self.stockNameEntry = ttk.Entry(self.searchStock)
		#self.stockNameEntry["show"] = "台積電"
		#self.stockNameEntry["fg"] = "gray"
		self.stockNameEntry["textvariable"] = self.stockNameStr
		self.stockNameEntry["width"] = 18
		self.mRegAdaptor.registerUI("stockNameEntry", (self.mRegAdaptor.getUIInfo("stockNameLabel").getRowLoc()+self.mRegAdaptor.getUIInfo("stockNameLabel").getRowSpan()), self.mRegAdaptor.getUIInfo("stockNameLabel").getColumnLoc(), 2, self.mRegAdaptor.getUIInfo("stockNameLabel").getColumnSpan())
		self.stockNameEntry.grid(row=self.mRegAdaptor.getUIInfo("stockNameEntry").getRowLoc(), column=self.mRegAdaptor.getUIInfo("stockNameEntry").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("stockNameEntry").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("stockNameEntry").getRowSpan(), sticky=tk.W+tk.N)
		self.stockNameEntry.grid_forget()
		
		# 設定按鈕容器frame位置
		self.searchBtFrame = tk.Frame(self)
		#self.searchBtFrame["heigh"] = 5
		#self.searchBtFrame["bg"] = "blue"
		self.searchBtFrame["height"] = 100
		self.searchBtFrame["width"] = 150
		self.searchBtFrame.configure(height=self.searchBtFrame["height"],width=self.searchBtFrame["width"])
		self.searchBtFrame.grid_propagate(0)
		self.mRegAdaptor.registerUI("searchBtFrame", (self.mRegAdaptor.getUIInfo("searchStockFrame").getRowLoc()+self.mRegAdaptor.getUIInfo("searchStockFrame").getRowSpan()), self.mRegAdaptor.getUIInfo("searchStockFrame").getColumnLoc(), int(self.mRegAdaptor.getUIInfo("dummyVsbBar").getRowSpan()*1/4), 2)
		self.searchBtFrame.grid(row=self.mRegAdaptor.getUIInfo("searchBtFrame").getRowLoc(), column=self.mRegAdaptor.getUIInfo("searchBtFrame").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("searchBtFrame").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("searchBtFrame").getRowSpan(), sticky=tk.N+tk.W)
		
		# 設定開始查詢按鈕
		self.button = ttk.Button(self.searchBtFrame)
		self.button["text"] = "開始查詢"
		#self.button["command"] = self.click_start(query_stock=self.app.query_stock(self.return_result_text, self.dateEntry.get()));
		#self.button.grid(row=1, column=11, columnspan=2, rowspan=2, padx=30)
		self.mRegAdaptor.registerUI("searchBtn", 0, 0, 2, 2)
		self.button.grid(row=self.mRegAdaptor.getUIInfo("searchBtn").getRowLoc(), column=self.mRegAdaptor.getUIInfo("searchBtn").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("searchBtn").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("searchBtn").getRowSpan(), sticky=tk.W+tk.S)
		
		# 設定等待動畫label
		self.showLoadingLabel = tk.Label(self)
		self.mRegAdaptor.registerUI("aniLabel", 198, 298, 2, 2)
		self.showLoadingLabel.grid(row=self.mRegAdaptor.getUIInfo("aniLabel").getRowLoc(), column=self.mRegAdaptor.getUIInfo("aniLabel").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("aniLabel").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("aniLabel").getRowSpan(), sticky=tk.E+tk.W+tk.S+tk.N)
		self.showLoadingLabel.grid_forget()
	
	def showItemOn(self, item_ptr, item_name):
		if ((len(item_ptr) == len(item_name))):
			mName = []
			for j in range(len(item_name)):
				mName.append(item_name[j])
			for i in range(len(item_ptr)):
				loc_adaptor = self.mRegAdaptor.getUIInfo(mName[i])
				#lc = self.mRegAdaptor.getUIInfo("yearLabel")
				#print(type(loc_adaptor))
				#print(type(item_name[i]))
				item_ptr[i].grid(row=loc_adaptor.getRowLoc(), column=loc_adaptor.getColumnLoc(), columnspan=loc_adaptor.getColumnSpan(), rowspan=loc_adaptor.getRowSpan(), sticky=tk.W+tk.N)
		else:
			print("length of item_ptr must be equal to item_name")
			
	def showItemOff(self, item_ptr):
		for i in range(len(item_ptr)):
			item_ptr[i].grid_forget()
				
	def taskForPrftLoss(self):
		self.searchItemInfo = "綜合損益彙總表"
		if self.getSrchModeInfo() == "股票篩選器":
			mItemPtr = [self.yearLabel, self.srchYearOpionMenu, self.seasonLabel, self.srchSeasonOpionMenu, self.firstCal, self.searchDateLabel, self.stockNameLabel, self.stockNameEntry]
			self.showItemOff(mItemPtr)
			self.stockFilterOptionVar = ["營業利益 > 100"]
			self.stockFilterOptionStr.set(self.stockFilterOptionVar[0])
			#self.stockFilterOpionMenu = ttk.OptionMenu(self.searchFrame, self.stockFilterOptionStr, self.stockFilterOptionVar[0], *self.stockFilterOptionVar, command=self.stock_filter_func, style="TMenubutton")
			mItemPtr = [self.stockFilterLabel, self.stockFilterOpionMenu]
			mItemName = ["StockFilterLabel","StockFilterOption"]
			self.showItemOn(mItemPtr, mItemName)
		else:
			mItemPtr = [self.searchDateLabel, self.yearLabel, self.srchYearOpionMenu, self.seasonLabel, self.srchSeasonOpionMenu]
			mItemName = ["SearchDateLabel", "yearLabel", "srchYearOpion", "seasonLabel", "srchSeasonOpion"]
			self.showItemOn(mItemPtr, mItemName)
			mItemPtr = [self.firstCal, self.stockFilterLabel, self.stockFilterOpionMenu]
			self.showItemOff(mItemPtr)
			if self.getSrchModeInfo() == "單一股票資訊":
				mItemPtr = [self.stockNameLabel, self.stockNameEntry]
				mItemName = ["stockNameLabel", "stockNameEntry"]
				self.showItemOn(mItemPtr, mItemName)
				
		#self.searchDateLabel.grid(row=self.mRegAdaptor.getUIInfo("SearchDateLabel").getRowLoc(), column=self.mRegAdaptor.getUIInfo("SearchDateLabel").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("SearchDateLabel").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("SearchDateLabel").getRowSpan(), sticky=tk.W)
		#self.firstCal.grid_forget()
		#self.srchSeasonOpionMenu.grid(row=self.mRegAdaptor.getUIInfo("srchSeasonOpion").getRowLoc(), column=self.mRegAdaptor.getUIInfo("srchSeasonOpion").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("srchSeasonOpion").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("srchSeasonOpion").getRowSpan(), sticky=tk.W)
		#self.yearLabel.grid()
		#self.srchYearOpionMenu.grid()
		#self.seasonLabel.grid()
		print(self.searchItemInfo)
		
	def taskForBalncSht(self):
		self.searchItemInfo = "資產負債彙總表"
		if self.getSrchModeInfo() == "股票篩選器":
			mItemPtr = [self.yearLabel, self.srchYearOpionMenu, self.seasonLabel, self.srchSeasonOpionMenu, self.firstCal, self.searchDateLabel, self.stockNameLabel, self.stockNameEntry]
			self.showItemOff(mItemPtr)
		else:
			mItemPtr = [self.searchDateLabel, self.yearLabel, self.srchYearOpionMenu, self.seasonLabel, self.srchSeasonOpionMenu]
			mItemName = ["SearchDateLabel", "yearLabel", "srchYearOpion", "seasonLabel", "srchSeasonOpion"]
			self.showItemOn(mItemPtr, mItemName)
			mItemPtr = [self.firstCal, self.stockFilterLabel, self.stockFilterOpionMenu]
			self.showItemOff(mItemPtr)
			if self.getSrchModeInfo() == "單一股票資訊":
				mItemPtr = [self.stockNameLabel, self.stockNameEntry]
				mItemName = ["stockNameLabel", "stockNameEntry"]
				self.showItemOn(mItemPtr, mItemName)
		#self.searchDateLabel.grid(row=self.mRegAdaptor.getUIInfo("SearchDateLabel").getRowLoc(), column=self.mRegAdaptor.getUIInfo("SearchDateLabel").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("SearchDateLabel").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("SearchDateLabel").getRowSpan(), sticky=tk.W)
		#self.firstCal.grid_forget()
		#self.srchSeasonOpionMenu.grid(row=self.mRegAdaptor.getUIInfo("srchSeasonOpion").getRowLoc(), column=self.mRegAdaptor.getUIInfo("srchSeasonOpion").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("srchSeasonOpion").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("srchSeasonOpion").getRowSpan(), sticky=tk.W)
		print(self.searchItemInfo)
		
	def taskForPrftAnlys(self):
		self.searchItemInfo = "營益分析彙總表"
		if self.getSrchModeInfo() == "股票篩選器":
			mItemPtr = [self.yearLabel, self.srchYearOpionMenu, self.seasonLabel, self.srchSeasonOpionMenu, self.firstCal, self.searchDateLabel, self.stockNameLabel, self.stockNameEntry]
			self.showItemOff(mItemPtr)
		else:
			mItemPtr = [self.searchDateLabel, self.yearLabel, self.srchYearOpionMenu, self.seasonLabel, self.srchSeasonOpionMenu]
			mItemName = ["SearchDateLabel", "yearLabel", "srchYearOpion", "seasonLabel", "srchSeasonOpion"]
			self.showItemOn(mItemPtr, mItemName)
			mItemPtr = [self.firstCal, self.stockFilterLabel, self.stockFilterOpionMenu]
			self.showItemOff(mItemPtr)
			if self.getSrchModeInfo() == "單一股票資訊":
				mItemPtr = [self.stockNameLabel, self.stockNameEntry]
				mItemName = ["stockNameLabel", "stockNameEntry"]
				self.showItemOn(mItemPtr, mItemName)
		#self.searchDateLabel.grid(row=self.mRegAdaptor.getUIInfo("SearchDateLabel").getRowLoc(), column=self.mRegAdaptor.getUIInfo("SearchDateLabel").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("SearchDateLabel").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("SearchDateLabel").getRowSpan(), sticky=tk.W)
		#self.firstCal.grid_forget()
		#self.srchSeasonOpionMenu.grid(row=self.mRegAdaptor.getUIInfo("srchSeasonOpion").getRowLoc(), column=self.mRegAdaptor.getUIInfo("srchSeasonOpion").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("srchSeasonOpion").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("srchSeasonOpion").getRowSpan(), sticky=tk.W)
		print(self.searchItemInfo)
		
	def taskForDayTrade(self):
		self.searchItemInfo = "股票交易資訊"
		if self.getSrchModeInfo() == "股票篩選器":
			mItemPtr = [self.yearLabel, self.srchYearOpionMenu, self.seasonLabel, self.srchSeasonOpionMenu, self.firstCal, self.searchDateLabel, self.stockNameLabel, self.stockNameEntry]
			self.showItemOff(mItemPtr)
		else:
			mItemPtr = [self.yearLabel, self.srchYearOpionMenu, self.seasonLabel, self.srchSeasonOpionMenu, self.stockFilterLabel, self.stockFilterOpionMenu]
			#mItemName = ["yearLabel", "srchYearOpion", "seasonLabel", "srchSeasonOpion"]
			self.showItemOff(mItemPtr)
			mItemPtr = [self.searchDateLabel, self.firstCal]
			mItemName = ["SearchDateLabel", "FirstDateCal"]
			self.showItemOn(mItemPtr, mItemName)
			if self.getSrchModeInfo() == "單一股票資訊":
				mItemPtr = [self.stockNameLabel, self.stockNameEntry]
				mItemName = ["stockNameLabel", "stockNameEntry"]
				self.showItemOn(mItemPtr, mItemName)
		print(self.searchItemInfo)
		
	def taskForAllStock(self):
		self.searchModeInfo = "所有股票資訊"
		if self.getSrchItemInfo() == "股票交易資訊":
			mItemPtr = [self.yearLabel, self.srchYearOpionMenu, self.seasonLabel, self.srchSeasonOpionMenu]
			#mItemName = ["yearLabel", "srchYearOpion", "seasonLabel", "srchSeasonOpion"]
			self.showItemOff(mItemPtr)
			mItemPtr = [self.searchDateLabel, self.firstCal]
			mItemName = ["SearchDateLabel", "FirstDateCal"]
			self.showItemOn(mItemPtr, mItemName)
		else:
			mItemPtr = [self.searchDateLabel, self.yearLabel, self.srchYearOpionMenu, self.seasonLabel, self.srchSeasonOpionMenu]
			mItemName = ["SearchDateLabel", "yearLabel", "srchYearOpion", "seasonLabel", "srchSeasonOpion"]
			self.showItemOn(mItemPtr, mItemName)
			mItemPtr = [self.firstCal]
			self.showItemOff(mItemPtr)

		mItemPtr = [self.stockNameLabel, self.stockNameEntry, self.stockFilterLabel, self.stockFilterOpionMenu]
		self.showItemOff(mItemPtr)
		#self.stockNameLabel.grid_forget()
		#self.stockNameEntry.grid_forget()
		print(self.searchModeInfo)
		
	def taskForOneStock(self):
		self.searchModeInfo = "單一股票資訊"
		if self.getSrchItemInfo() == "股票交易資訊":
			mItemPtr = [self.yearLabel, self.srchYearOpionMenu, self.seasonLabel, self.srchSeasonOpionMenu]
			#mItemName = ["yearLabel", "srchYearOpion", "seasonLabel", "srchSeasonOpion"]
			self.showItemOff(mItemPtr)
			mItemPtr = [self.searchDateLabel, self.firstCal]
			mItemName = ["SearchDateLabel", "FirstDateCal"]
			self.showItemOn(mItemPtr, mItemName)
		else:
			mItemPtr = [self.searchDateLabel, self.yearLabel, self.srchYearOpionMenu, self.seasonLabel, self.srchSeasonOpionMenu]
			mItemName = ["SearchDateLabel", "yearLabel", "srchYearOpion", "seasonLabel", "srchSeasonOpion"]
			self.showItemOn(mItemPtr, mItemName)
			mItemPtr = [self.firstCal]
			self.showItemOff(mItemPtr)
			
		mItemPtr = [self.stockNameLabel, self.stockNameEntry]
		mItemName = ["stockNameLabel", "stockNameEntry"]
		self.showItemOn(mItemPtr, mItemName)
		
		mItemPtr = [self.stockFilterLabel, self.stockFilterOpionMenu]
		self.showItemOff(mItemPtr)
		#self.stockNameLabel.grid(row=self.mRegAdaptor.getUIInfo("stockNameLabel").getRowLoc(), column=self.mRegAdaptor.getUIInfo("stockNameLabel").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("stockNameLabel").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("stockNameLabel").getRowSpan(), sticky=tk.W)
		#self.stockNameEntry.grid(row=self.mRegAdaptor.getUIInfo("stockNameEntry").getRowLoc(), column=self.mRegAdaptor.getUIInfo("stockNameEntry").getColumnLoc(), columnspan=self.mRegAdaptor.getUIInfo("stockNameEntry").getColumnSpan(), rowspan=self.mRegAdaptor.getUIInfo("stockNameEntry").getRowSpan(), sticky=tk.W)
		print(self.searchModeInfo)
		
	def taskForStockAnlys(self):
		self.searchModeInfo = "股票篩選器"
		mItemPtr = [self.stockFilterLabel, self.stockFilterOpionMenu]
		mItemName = ["StockFilterLabel","StockFilterOption"]
		self.showItemOn(mItemPtr, mItemName)
		mItemPtr = [self.yearLabel, self.srchYearOpionMenu, self.seasonLabel, self.srchSeasonOpionMenu, self.firstCal, self.searchDateLabel, self.stockNameLabel, self.stockNameEntry]
		#mItemName = ["yearLabel", "srchYearOpion", "seasonLabel", "srchSeasonOpion"]
		self.showItemOff(mItemPtr)
		print(self.searchModeInfo)
	
	def taskForDefault(self):
		self.searchModeInfo = self.searchModeOptionVar[0]
		self.searchItemInfo = self.searchOptionVar[0]
		self.searchSeasonInfo = self.srchSeasonOptionVar[0]
		self.searchYearInfo = self.srchYearOptionVar[0]
		self.searchDateInfo = "".join(str(self.firstCal.get_date()).split('-'))
		#print("no this option!")
	
	def search_item_func(self, value):
		self.searchItemDic.get(value,self.taskForDefault)()
		
	def search_mode_func(self, value):
		self.searchModeDic.get(value,self.taskForDefault)()
		
	def search_year_func(self, value):
		self.searchYearInfo = value
		print(self.searchYearInfo)
		
	def search_season_func(self, value):
		if value == "第一季":
			self.searchSeasonInfo = 1
		elif value == "第二季":
			self.searchSeasonInfo = 2
		elif value == "第三季":
			self.searchSeasonInfo = 3
		elif value == "第四季":
			self.searchSeasonInfo = 4
		print(self.searchSeasonInfo)
		
	def first_cal_sel(self, value):
		self.searchDateInfo = "".join(str(self.firstCal.get_date()).split('-'))
		print(self.searchDateInfo)
		
	#def end_cal_sel(self, value):
	#	print(self.searchDateInfo)
	
	def getRegAdaptor(self):
		return self.mRegAdaptor
	
	def getSrchItemInfo(self):
		return self.searchItemInfo
		
	def getSrchModeInfo(self):
		return self.searchModeInfo
		
	def getSrchSeasonInfo(self):
		return self.searchSeasonInfo
		
	def getSrchDateInfo(self):
		return self.searchDateInfo
		
	def getSrchYearInfo(self):
		return self.searchYearInfo
	
	def getStockName(self):
		return self.stockNameEntry.get()
		
	def getRsltWinText(self):
		return self.text
		
	def getAnimationLabel(self):
		return self.showLoadingLabel
		
	def getSrchButton(self):
		return self.button
	
	def getSrchDftModeInfo(self):
		return self.searchModeOptionVar[0]
	
	def getSrchDftItemInfo(self):
		return self.searchOptionVar[0]
	
	def getSrchDftYearInfo(self):
		return self.srchYearOptionVar[27]
		
	def getSrchDftSeasonInfo(self):
		return self.srchSeasonOptionVar[0]
		
	def getSrchDateInfo(self):
		return "".join(str(self.firstCal.get_date()).split('-'))
		
	def stock_filter_func(self, value):
		self.stock_filter_option = value
	
	def getStockFilterOption(self):
		return self.stock_filter_option