#! /bin/env python
# -*- coding:utf-8 -*-
#-------------------------------------------------------------------------------
# Name:        Packaging script
# Purpose:
#
# Author:      liangyu.zhang
#
# Created:     28/11/2016
# Copyright:   (c) liangyu.zhang 2016
# Licence:     <your licence>
#-------------------------------------------------------------------------------
import os,sys,time
import ConfigParser
import shutil
import datetime
import ctypes,sys




STD_INPUT_HANDLE = -10
STD_OUTPUT_HANDLE = -11
STD_ERROR_HANDLE = -12
FOREGROUND_DARKGREEN = 0x02
FOREGROUND_DARKRED = 0x04
FOREGROUND_SKYBLUE = 0x0b
std_out_handle = ctypes.windll.kernel32.GetStdHandle(STD_OUTPUT_HANDLE)


_dict  = {}


''' 终端打印颜色 '''
class LogColor:

        @staticmethod
        def set_cmd_text_color(color, handle=std_out_handle):
            Bool = ctypes.windll.kernel32.SetConsoleTextAttribute(handle, color)
            return Bool

        @staticmethod
        def resetColor():
            LogColor.set_cmd_text_color(FOREGROUND_DARKRED | FOREGROUND_DARKGREEN|FOREGROUND_SKYBLUE)

        @staticmethod
        def printGreen(mess):
            LogColor.set_cmd_text_color(FOREGROUND_DARKGREEN)
            sys.stdout.write(mess)
            LogColor.resetColor()
            print

        @staticmethod
        def printRed(mess):
            LogColor.set_cmd_text_color(FOREGROUND_DARKRED)
            sys.stdout.write(mess)
            LogColor.resetColor()
            print

        @staticmethod
        def printBule(mess):
            LogColor.set_cmd_text_color(FOREGROUND_SKYBLUE)
            sys.stdout.write(mess)
            LogColor.resetColor()
            print


''' 打包类 '''
class Packaging(object):


    ''' 初始化参数'''
    def __init__(self,file):
        self.file = file
        self._Config()

    ''' 读取配置文件方法 '''
    def _Config(self):

        config = ConfigParser.ConfigParser()
        try:
            config.read(self.file)
            secs = config.sections()
            for l in secs:
                try:
                    copy = config.get(l,'copy')
                    app_del = config.get(l,'app-del')
                    kom2_del   = config.get(l,'kom2-del')
                    arrg = {'copy':copy,'app_del':app_del,'kom2_del':kom2_del}
                    _dict[l] = arrg
                except ConfigParser.NoSectionError:
                    continue
            #return _dict
        except IOError:
            LogColor.printRed ("Error: %s  Cannot find file"%self.file)
            sys.exit(2)

    ''' 时间方法 '''
    @staticmethod
    def DateTime():
        today = datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        return today

    ''' 删除文件或目录方法 '''
    def  RemoveFile(self,obj,file):
        _list = list(eval(file))
        for i in _list:
            file = r'%s\%s'%(obj,i)
            if  os.path.isfile(file):
                os.remove(file)
            elif os.path.exists(file):
                shutil.rmtree(file)
            else:
                LogColor.printRed ("%s Error: `%s` file or directory does not exist or deleted"%(Packaging.DateTime(),file))
                continue
            LogColor.printGreen("%s INFO `%s` delete file or directory success"%(Packaging.DateTime(),file))



    ''' 复制或覆盖文件目录方法 '''
    def CopyFile(self,sobj,dobj,file):
        _list = list(eval(file))
        for i in _list:
            sdir = r'%s\%s'%(sobj,i)
            pdir = r'%s\%s'%(dobj,i)
            if os.path.exists(sdir):
                if not os.path.exists(pdir):
                    os.makedirs(pdir)
                comm = "xcopy %s %s /y /e /Q"%(sdir,pdir)
                if (os.system(comm)) != 0:
                    LogColor.printRed( "%s Error Copy '%s' to '%s' copy failed"%(Packaging.DateTime(),sdir,pdir))
                else:
                    LogColor.printGreen("%s INFO Copy '%s' to '%s' sucess"%(Packaging.DateTime(),sdir,pdir))
            else:
               LogColor.printRed("%s Error `%s` does not exist"%(Packaging.DateTime(),sdir))


''' 主方法调用 '''
def MainFun():
    init = Packaging(file)
    channel_all = channel.split(',')
    for ch  in channel_all:
        value = _dict.get(ch)
        if not value:
            LogColor.printRed( "%s Error: Channel %s key does not exist"%(Packaging.DateTime(),ch))
            continue

        cdir = r'%s\%s\%s'%(dir_path,ch,dapk)
        if not os.path.exists(cdir):
            os.makedirs(cdir)
        pdir = r'%s\sources\%s'%(dir_path,dapk)
        if os.path.exists(pdir):
            comm = 'xcopy %s %s /y /e /Q'%(pdir,cdir)
            if (os.system(comm)) != 0:
                LogColor.printRed ('%s Error `%s` Copy directory failed'%(Packaging.DateTime(),pdir))
        else:
            LogColor.printRed ("%s Error `%s` directory does not exist"%(Packaging.DateTime(),pdir))
            continue

        keys = value.keys()

        if 'app_del' in keys:
            pdir = '%s\sources\%s-%s'%(dir_path,sapk,ch)
            v = value.get('app_del')
            init.RemoveFile(pdir,v)
            LogColor.printGreen ("-" *  165)

        if 'kom2_del' in keys:
            v = value.get('kom2_del')
            init.RemoveFile(cdir,v)
            LogColor.printGreen ("-" *  165)

        if 'copy' in keys:
            pdir = '%s\sources\%s-%s'%(dir_path,sapk,ch)
            v = value.get('copy')
            init.CopyFile(pdir,cdir,v)
            LogColor.printGreen ("-" *  165)

            LogColor.printBule("Channel %s Packaging complete..."%ch)




if __name__ == '__main__':
    file = os.path.abspath(os.path.dirname(sys.argv[0]))+'/config.ini'
    dir_path = "D:\\packages\\ylzhs"
    if len(sys.argv) != 4:
        LogColor.printGreen('''Uages explain:
        parameter one  'apk source directory'
        parameter two  'apk target  directory'
        parameter three  'channel id or channel 1,2,3'
        ''')
        sys.exit()

    sapk = sys.argv[1]
    dapk = sys.argv[2]
    channel = sys.argv[3]
    MainFun()

