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
            var title = $translate.instant('register.success_title');
            var msg = $translate.instant('register.success_message');
            var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
            $dialog.messageBox(title, msg, btns).then(function() {
                console.log('Dialog confirmed, navigating to login...');
                $scope.user = {}; // clean up all input
                $state.go('login');
            });
        }, function(data) {
            var title = $translate.instant('register.error_title');
            var msg = $translate.instant('register.error_message');
            var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
            $dialog.messageBox(title, msg, btns);
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
 */

// Error: 
// jakarta.servlet.ServletException: 
// jakarta.servlet.ServletException: 
// org.hibernate.PropertyValueException: 
// not-null property references a null or transient value : 
// com.sismics.docs.core.model.jpa.User.storageQuota