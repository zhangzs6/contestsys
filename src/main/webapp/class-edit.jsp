<!DOCTYPE html>
<html class="x-admin-sm">
<head>
<%@page isELIgnored="false"%>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<meta charset="UTF-8">
<title>欢迎页面-X-admin2.2</title>
<meta name="renderer" content="webkit">
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
<meta name="viewport"
	content="width=device-width,user-scalable=yes, minimum-scale=0.4, initial-scale=0.8,target-densitydpi=low-dpi" />
<link rel="stylesheet" href="./css/font.css">
<link rel="stylesheet" href="./css/xadmin.css">
<link rel="stylesheet" href="lib/layui/css/layui.css">
<script type="text/javascript" src="./assets/js/jquery-1.8.3.min.js" ></script>
<script type="text/javascript" src="./lib/layui/layui.js"
	charset="utf-8"></script>
<script type="text/javascript" src="./js/xadmin.js"></script>
<!-- 让IE8/9支持媒体查询，从而兼容栅格 -->
<!--[if lt IE 9]>
            <script src="https://cdn.staticfile.org/html5shiv/r29/html5.min.js"></script>
            <script src="https://cdn.staticfile.org/respond.js/1.4.2/respond.min.js"></script>
        <![endif]-->
</head>
<%
	request.setCharacterEncoding("UTF-8");
%>
<body>
	<div class="layui-fluid">
		<div class="layui-row">
			<form class="layui-form">
				
				<!-- 无用div控制布局作用 -->
               	<div style="height: 30px">
               	</div>
                	

				<div class="layui-form-item">
					<label for="classId" class="layui-form-label"> <span
						class="x-red">*</span>班级id
					</label>
					<div class="layui-input-inline">
						<input type="text" id="classId" name="classId" required=""
							lay-verify="required" autocomplete="off" class="layui-input"
							value="${classes.classId}" disabled="disabled"> 
					</div>
					<div class="layui-form-mid layui-word-aux">
						<span class="x-red">*</span>
					</div>
				</div>


				<div class="layui-form-item">
					<label for="name" class="layui-form-label"> <span
						class="x-red">*</span>班级名称
					</label>
					<div class="layui-input-inline">
						<input type="text" id="name" name="name" required=""
							lay-verify="required" autocomplete="off" class="layui-input"
							value="${classes.name}">
					</div>
					<div class="layui-form-mid layui-word-aux">
						<span class="x-red">*</span>
					</div>
				</div>

				<div class="layui-form-item">
					<label class="layui-form-label"></label>
					<button class="layui-btn" lay-filter="add" lay-submit="">更新</button>
				</div>
			</form>
		</div>
	</div>
	<script>layui.use([ 'form', 'layer' ],
			function() {
				$ = layui.jquery;
				var form = layui.form,
					layer = layui.layer;
	
	
				//监听提交
				form.on('submit(add)',
					function(data) {
	
	
						$.ajax({
							type : "POST",
							url : "Class/UpdateClass",
							dataType : 'json',
							data : data.field,
							async : false,
							success : function(da) {
								console.log(JSON.stringify(da))
								if (da.code == 0) {
									layer.alert(da.msg, {
										icon : 6
									},
										function() {
											//关闭当前frame
											xadmin.close();
	
											// 可以对父窗口进行刷新 
											xadmin.father_reload();
										});
	
	
								} else {
									layer.alert(da.msg, {
										icon : 5
									});
	
								}
							}
						})
						return false;
	
	
					});
	
			});
	</script>
	<script>var _hmt = _hmt || [];
		(function() {
			var hm = document.createElement("script");
			hm.src = "https://hm.baidu.com/hm.js?b393d153aeb26b46e9431fabaf0f6190";
			var s = document.getElementsByTagName("script")[0];
			s.parentNode.insertBefore(hm, s);
		})();
	</script>
		<script type="text/javascript">
			$(document).ready(function() {
					loadingUserName();
			});
		
			
			/* 显示登录的用户名 */
		      function loadingUserName() {
		      		$.ajax({
								type: "POST", 
								url: "User/getUsername",
								data: '',
								async: false,
								dataType: 'json', 
								success: function(data) {
									if(data.level<2) {	//判断有误权限进来此页面
										alert("无访问权限");
										window.location.href = "./error.html";
									}
								}
							})
		      }
		
		</script>
</body>

</html>