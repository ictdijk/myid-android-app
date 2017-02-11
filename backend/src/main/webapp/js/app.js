(function() {
    'use strict';

    angular.module('MyID', [])
        .controller('MyIDController', MyIDController);

    MyIDController.$inject = ['$scope'];
    function MyIDController($scope) {
        $scope.targetUrl = "https://mijd-jwt.appspot.com/";
		$scope.audience="/root/nl";
    }
})();