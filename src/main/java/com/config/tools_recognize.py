# -*- coding: utf-8 -*-
"""
Created on Fri Mar 29 17:46:00 2019
#输入：图片路径字符串（注意字符串里的路径是\\形式）
#输出：json结构的结果out_data，其中'num_tools'为工具数量;'edge_near'值为1代表有靠近边缘的工具；'tools_near'值为1表示有靠的太近（或重叠）的工具。
#0404修改，输入改为路径字符串
##python->jar
@author: hedelu
"""

import cv2
import numpy as np
import json
import sys

def tools_recognize(filepath):
#    test1 = base64.b64decode(res)
#    file = open('test.jpg','wb')
#    file.write(test1)
#    file.close()
    imgsrc = cv2.imread(filepath,cv2.IMREAD_COLOR)
    color_imgsrc416 = cv2.resize(imgsrc,(416,416),interpolation=cv2.INTER_AREA)
    gray_imgsrc416 = cv2.cvtColor(color_imgsrc416,cv2.COLOR_BGR2GRAY)
    filer_gray416 = cv2.bilateralFilter(gray_imgsrc416,9,15,150)
    edges = cv2.Canny(filer_gray416,100,200)
    img_hsv = cv2.cvtColor(color_imgsrc416,cv2.COLOR_BGR2HSV)
    for i in range(edges.shape[0]):
        for j in range(edges.shape[1]):
            if img_hsv[i, j, 1] > 127 and img_hsv[i, j, 2] > 127:
                edges[i, j] = 255
    morph_kernal = cv2.getStructuringElement(cv2.MORPH_RECT,(5,5))
    edges_morph = cv2.dilate(edges, morph_kernal)
    ret, edges_binary = cv2.threshold(edges_morph,127,255,cv2.THRESH_BINARY)
#    img_contours, contours, hierarchy = cv2.findContours(edges_binary,cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_NONE)#旧版本输出三个参数
    contours, hierarchy = cv2.findContours(edges_binary,cv2.RETR_EXTERNAL,cv2.CHAIN_APPROX_NONE)#新版本输出2个参数，要想返回三个参数：把OpenCV 降级成3.4.3.18 就可以了，在终端输入pip install opencv-python==3.4.3.18
    num_tools = len(contours)
    near_edge = 0
    contours_left = []
    for i in range(num_tools):
        boundary = contours[i]
        min_boundary = np.min(boundary)
        max_boundary = np.max(boundary)
        if boundary.shape[0] <= 80:
            num_tools = num_tools - 1
        elif min_boundary <= 3 or 415 - max_boundary <= 3:
            num_tools = num_tools - 1
            near_edge = 1
        else:
            contours_left.append(boundary)
            
            
    # 距离太近的轮廓
    len_contours_left = len(contours_left)
    outnear_flag = 0#有靠的太近的工具
    for i in range(len_contours_left):
        near_flag = 0
        contours_i = contours_left[-1]
        contours_left.pop()
        for j in range(len(contours_left)):       
            for k in range(contours_i.shape[0]):
                for l in range(contours_left[j].shape[0]):
                    if abs(contours_i[k,0,0] - contours_left[j][l,0,0]) + abs(contours_i[k,0,1] - contours_left[j][l,0,1]) < 6:
                        near_flag = 1
                        outnear_flag = 1
                        continue
        if near_flag == 1:
            num_tools = num_tools - 1
        
        
        
    out_data = {}
    out_data['num_tools'] = num_tools
    out_data['edge_near'] = near_edge
    out_data['tools_near'] = outnear_flag
    out_data = json.dumps(out_data)
#    print('成功！')
    print(out_data)
    return out_data
filepath = sys.argv[1]#获取外部调用参数
#filepath = 'D:\\algorithm\\tools_recognise\\1.jpg'
aa = tools_recognize(filepath)

