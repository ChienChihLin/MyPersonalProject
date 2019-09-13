import tkinter as tk
from stockapp import Application

if __name__ == '__main__':
	mainWin = tk.Tk()
	app = Application(mainWin)
	mainWin.mainloop()