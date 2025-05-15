'use strict';

/**
 * Register controller.
 */
angular.module('docs').controller('Register', function(Restangular, $scope, $rootScope, $state, $dialog, $translate) {
    $scope.user = {};

    // Add function to check the password
    $scope.validatePasswords = function() {
        if ($scope.user.password !== $scope.user.confirmPassword) {
            var title = $translate.instant('register.error_title');
            var msg = $translate.instant('register.password_mismatch');
            var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
            $dialog.messageBox(title, msg, btns);
            return false;
        }
        return true;
    };

    // Register
    $scope.register = function() {
        // check the password
        if (!$scope.validatePasswords()) {
            return;
        }
        
        Restangular.one('user').post('request_registration', {
            username: $scope.user.username,
            password: $scope.user.password,
            email: $scope.user.email
        }).then(function() {
            // Clear the form immediately
            $scope.user = {};
            if ($scope.registerForm) {
                $scope.registerForm.$setPristine();
                $scope.registerForm.$setUntouched();
            }
            
            var title = $translate.instant('register.success_title');
            var msg = $translate.instant('register.success_message');
            var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
            
            // Show success dialog and navigate
            $dialog.messageBox(title, msg, btns).then(function() {
                $state.go('login', {}, { location: 'replace' });
            });
        }, function(data) {
            // Clear the form immediately
            $scope.user = {};
            if ($scope.registerForm) {
                $scope.registerForm.$setPristine();
                $scope.registerForm.$setUntouched();
            }
            
            var title = $translate.instant('register.error_title');
            var msg = $translate.instant('register.error_message');
            var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
            $dialog.messageBox(title, msg, btns);
        }).catch(function(error) {
            console.error('Error: ', error);
        });
    };
});

/**
 * 第一次尝试注册：
 * 用户名：gxy
 * 密码：gxygxygxy
 * 邮箱：gxy@mail.com
 * 
 * 第二次尝试注册：
 * 用户名：gxy2
 * 密码：88888888
 * 邮箱：gxy2@mail.com
 * 
 * 第三次尝试注册：
 * 用户名：gxy3
 * 密码：gxy333333
 * 邮箱：gxy3@mail.com
 * 
 * 第四次尝试注册：
 * 用户名：gxy4
 * 密码：gxy444444
 * 邮箱：gxy4@mail.com
 */