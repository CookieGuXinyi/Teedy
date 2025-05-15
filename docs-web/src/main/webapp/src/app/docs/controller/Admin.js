'use strict';

/**
 * Admin controller.
 */
angular.module('docs').controller('Admin', function(Restangular, $scope, $rootScope, $state, User) {
    $scope.pendingRequests = [];

    // Get all pending registration requests
    $scope.loadPendingRequests = function() {
        console.log('Getting pending registration requests...');
        Restangular.one('user', 'list_pending_requests').get().then(function(data) {
            console.log('Got pending registration requests');
            console.log('Received data:', data.requests);
            console.log('Received length:', data.requests.length);
            $scope.pendingRequests = data.requests;
        }).catch(function(error) {
            console.error('Error:', error);
        });
    };

    // Call loadPendingRequests on controller initialization
    $scope.loadPendingRequests();  // Ensure this gets called to fetch data

    $scope.approveRequest = function(username, approve) {
        Restangular.one('user').post('approve_registration', {
            username: username,
            approve: approve
        }).then(function() {
            alert(approve ? 'Request approved.' : 'Request rejected.');
            $scope.loadPendingRequests(); // Reload the list
        }).catch(function(error) {
            console.error('Error: ', error);
        });
    };
});
