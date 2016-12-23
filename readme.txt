*First commit
*完成HandlerThread自动下载图片，并与发送消息给主线程进行更新
*不明白为什么在position:0 的位置会被重复调用getView
*修复bug
*创建搜索对话框
*完成搜索功能
*图片下载失败就重新下载，最多重试3次
*添加后台服务，查找返回最新结果
*添加定时器
*后台服务弹出通知消息
*随系统重启而开启的定时器
*过滤前台通知
	1.发送broadcast intent
	2.使用私有权限及使用ordered broadcast接收结果