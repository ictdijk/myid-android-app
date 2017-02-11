(function() {
    'use strict';

    angular.module('MyID', [])
        .controller('MyIDController', MyIDController);

    MyIDController.$inject = ['$scope'];
    function MyIDController($scope) {
        $scope.targetUrl = "https://mijd-jwt.appspot.com/";
		$scope.audience = "/university/amu";
		$scope.session = Math.floor((Math.random()*10000)+10000);
		$scope.loginUrl= window.encodeURIComponent("https://mijd-jwt.appspot.com/login?authenticate=" + window.encodeURIComponent("https://mijd-jwt.appspot.com/login?session="+$scope.session));
    }
})();