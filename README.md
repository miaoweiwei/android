# android App

## 搜索设备
### UDP广播
设备端口：777

手机发送UDP广播 发送信息**"Mobile"**
吸乳器收到手机的信息后返回吸 **乳器设备的名字和电量**
手机收到返回的信息然后根据收到的信息显示在手机的设备列表里

## 连接设备
### TCP连接设备
TCP端口：888
手机端点击设备列表里的一个设备开始连接设备，

首先判断手机是否已经连接设备，如果当前连接的设备正是点击的设备，给出提示如果不是也给出提示确认切换连接。

如果没有连接，就直接去连接设备。

### 心跳设置

手机与设备连接成功之后每**10秒**发送一次心跳，心跳消息为**"\*"** ，设备收到*之后返回手机设备的当前电量

### 控制

手机发送信息格式说明：
<table width="960" align="center" border="1" rules="all" cellpadding="15">
	<tr align="center">
		<th></th>
		<th>消息头</th>
		<th>第一位</th>
		<th>第二位</th>
		<th>第三位</th>
		<th>第四位</th>
		<th>第五位</th>
	</tr>
	<tr align="center">
		<td>例子</td>
		<td>zpf</td>
		<td>加按键</td>
		<td>减按键</td>
		<td>频率/强度切换按键</td>
		<td>模式切换按键</td>
		<td>开/关机按键</td>
	</tr>
	<tr align="center">
		<td>例如：手机按下加按键</td>
		<td>zpf</td>
		<td>1</td>
		<td>0</td>
		<td>0</td>
		<td>0</td>
		<td>0</td>
	</tr>
	<tr align="center">
		<td>例如：手机按下开机按键</td>
		<td>zpf</td>
		<td>0</td>
		<td>0</td>
		<td>0</td>
		<td>0</td>
		<td>1</td>
	</tr>
</table>

设备返回信息格式说明：
<table width="960" align="center" border="1" rules="all" cellpadding="15">
	<tr align="center">
		<th></th>
		<th>消息头</th>
		<th>第一位</th>
		<th>第二位</th>
		<th>第三位</th>
		<th>第四位</th>
		<th>第五位</th>
	</tr>
	<tr align="center">
		<td>例子</td>
		<td>zpf</td>
		<td>强度</td>
		<td>频率</td>
		<td>当前操作的是频率还是强度,0为频率，1为强度</td>
		<td>模式</td>
		<td>开/关机状态,0:未开机1:开机</td>
	</tr>
	<tr align="center">
		<td>例如：当前强度为5频率为3操作强度，模式三已开机</td>
		<td>zpf</td>
		<td>5</td>
		<td>3</td>
		<td>1</td>
		<td>3</td>
		<td>1</td>
	</tr>
</table>