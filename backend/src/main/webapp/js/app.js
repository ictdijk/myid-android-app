(function() {
    'use strict';

    var myIdApp = angular.module('MyID', ['ng'])
       .factory('RetryRequest', ['$http', '$q', function($http, $q) {
         return function(path) {
           var MAX_REQUESTS = 3,
               counter = 1,
               results = $q.defer();

           var request = function() {
             $http({method: 'GET', url: path})
               .success(function(response) {
                 results.resolve(response)
               })
               .error(function() {
                 if (counter < MAX_REQUESTS) {
                   request();
                   counter++;
                 } else {
                   results.reject("Could not load after multiple tries");
                 }
               });
           };

           request();

           return results.promise;
         }
       }]);

    angular.controller('MyIDController', function($scope, $location, $window, RetryRequest) {
        $scope.targetUrl = "https://mijd-jwt.appspot.com/";
		$scope.audience = "/university/amu";
		$scope.session = Math.floor((Math.random()*10000)+10000);
		$scope.sessionUrl = "https://mijd-jwt.appspot.com/authenticate/?session="+$scope.session
		$scope.loginUrl= window.encodeURIComponent($scope.sessionUrl);
		RetryRequest(sessionUrl).then(function(token) {

		    $window.location.href = 'https://mijd-jwt.appspot.com/';
        });
    });

})();