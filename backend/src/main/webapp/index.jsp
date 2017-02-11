<%@ page session="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ page import="in.yagnyam.myid.*"%>

<%
pageContext.setAttribute("results", Utils.getClaims(request));
%>

<!DOCTYPE html>
<html>
<head>
	<link rel="alternate" href="android-app://in.yagnyam.myid/https/mijd-jwt.appspot.com" />
    <title>MijD</title>
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap.min.css">
    <link rel="stylesheet" href="//netdna.bootstrapcdn.com/bootstrap/3.3.7/css/bootstrap-theme.min.css">
</head>



<body ng-app="MyID" role="document" style="padding-top: 70px;">
<div ng-controller="MyIDController as myIdController">

<div class="navbar navbar-inverse navbar-fixed-top" role="navigation">
    <div class="container">
        <div class="navbar-header">
            <a class="navbar-brand" href="#">MijD Demo</a>
        </div>
		<!--
        <div class="navbar-collapse collapse">
            <ul class="nav navbar-nav navbar-right">
                <li><a href="https://mijd-jwt.appspot.com/login?ret=https%3A%2F%2Fmijd-jwt.appspot.com%2F">Web Login</a></li>
                <li><a href="mijd://mijd-jwt.appspot.com/login?ret=https%3A%2F%2Fmijd-jwt.appspot.com%2F">MijD Login</a></li>
            </ul>
        </div>
		-->
    </div>
</div>

<div class="container theme-showcase" role="main">
			<h1>Amsterdam University</h1>
			<section>
                <div class="panel panel-default">
                    <div class="panel-heading">
                        <h3 class="panel-title">Authentication Token</h3>
                    </div>
                    <div class="panel-body">
                        <table class="table table-striped">
                            <thead>
                                <tr>
                                    <th>Attribute</th>
                                    <th>Value</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="i" items="${results}">
                                    <tr>
                                        <td>${i.key}</td>
                                        <td>${i.value}</td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
		    <section>

		
		
                    <div class="input-group">
						<label for="targetUrl">Site to Launch/Authorize:</label>
                        <input type="text" class="form-control input-lg" placeholder="Target URL" name="targetUrl" ng-model="targetUrl"></input>
                    </div>
                    <div class="input-group">
						<label for="audience">Audience/Invoker:</label>
                        <input type="text" class="form-control input-lg" placeholder="Audience" name="audience" ng-model="audience"></input>
                    </div>
					
					<!--div class="form-group">
                        <label for="privateKey">Private Key (Only required if Audience/Invoker is set):</label>
                        <textarea rows="10" cols="50" class="form-control input-lg" name="privateKey" placeholder="Private Key" ng-model="privateKey"></textarea>
                    </div-->

					<br/>
						<a href="https://mijd-jwt.appspot.com/login?ret={{targetUrl}}&audience={{audience}}" class="btn btn-info" role="button">Login</a>
					<br/>
					<!--
					MijD URL: <a href="mijd://mijd-jwt.appspot.com/login?ret={{targetUrl}}&audience={{audience}}">mijd://mijd-jwt.appspot.com/login?ret={{targetUrl}}&audience={{audience}}</a>
					<br/>
					-->
</div>

</div>

<script src="https://ajax.googleapis.com/ajax/libs/jquery/3.1.1/jquery.min.js"></script>
<script src="https://ajax.googleapis.com/ajax/libs/angularjs/1.5.7/angular.min.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.7/js/bootstrap.min.js"></script>
<script src="js/app.js"></script>

</body>
</html>
