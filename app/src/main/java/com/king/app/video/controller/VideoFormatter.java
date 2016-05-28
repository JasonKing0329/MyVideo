package com.king.app.video.controller;

public class VideoFormatter {

	public static String formatTime(long time) {
		String result = "";
		int hour = 0;
		int minute = 0;
		int second = 0;
		second = (int) (time / 1000);
		minute = second / 60;
		hour = minute / 60;

		minute = minute % 60;
		second = second % 60;
		if (hour < 10) {
			result = result + "0" + hour;
		}
		else {
			result = result + hour;
		}
		result = result + ":";
		if (minute < 10) {
			result = result + "0" + minute;
		}
		else {
			result = result + minute;
		}
		result = result + ":";
		if (second < 10) {
			result = result + "0" + second;
		}
		else {
			result = result + second;
		}
		return result;
	}

	public static String formatSize(long size) {

		String result = null;
		long temp = size;
		long kb = temp >> 10;
		long mb = temp >> 20;
		long gb = temp >> 30;
		if (gb == 0) {
			if (mb == 0) {
				/*
				 * 方法1:用Math.round计算,这里返回的数字格式的.
					float price=89.89;
					int itemNum=3;
					float totalPrice=price*itemNum;
					float num=(float)(Math.round(totalPrice*100)/100);//如果要求精确4位就*10000然后/10000

					 方法2:用DecimalFormat 返回的是String格式的.该类对十进制进行全面的封装.像%号,千分位,小数精度.科学计算.
					float price=1.2;
					DecimalFormat decimalFormat=new DecimalFormat(".00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
					String p=decimalFomat.format(price);//format 返回的是字符串
				 */
				if (kb == 0) {
					result = size + "B";
				}
				else {
					float point = (float)(temp%1024)/1024f;
					int nPoint = (int) (100 * point);
					if (nPoint < 10) {
						result = kb + ".0" + nPoint + "KB";
					}
					else {
						result = kb + "." + nPoint + "KB";
					}
				}
			}
			else {
				temp = temp - (mb << 20);
				double point = (double) temp / (double) (((long) 2) << 20);
				int nPoint = (int) (100 * point);
				if (nPoint < 10) {
					result = mb + ".0" + nPoint + "MB";
				}
				else {
					result = mb + "." + nPoint + "MB";
				}
			}
		}
		else {
			temp = temp - (gb << 30);
			double point = (double) temp / (double) (((long) 2) << 30);
			int nPoint = (int) (100 * point);
			if (nPoint < 10) {
				result = gb + ".0" + nPoint + "GB";
			}
			else {
				result = gb + "." + nPoint + "GB";
			}
		}

		return result;
	}

}
