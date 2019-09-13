import sys
from tkinter import messagebox as msg

class ShowMessageBox():
	def __init__(self):
		self.message_option = True
	
	def capture_exception(self):
		type, message, traceback = sys.exc_info()
		while traceback:
			print('..................................')
			print(type)
			print(message)
			print('function or module？', traceback.tb_frame.f_code.co_name)
			print('file？', traceback.tb_frame.f_code.co_filename)
			traceback = traceback.tb_next
			
	def show_mesg_box(self, title, show_info):
		if title == "Info":
			msg.showinfo(title, show_info)
		elif title == "Warning":
			msg.showwarning(title, show_info)
		elif title == "Error":
			msg.showerror(title, show_info)
		else:
			msg.showerror("Input Error", "Please Input Correct Parameters!!")