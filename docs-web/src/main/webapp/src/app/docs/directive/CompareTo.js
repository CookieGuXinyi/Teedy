'use strict';

angular.module('docs').directive('compareTo', function() {
    return {
        require: "ngModel",
        scope: {
            otherModelValue: "=compareTo"
        },
        link: function(scope, element, attributes, ngModel) {
            // check the password
            ngModel.$validators.compareTo = function(modelValue) {
                return modelValue == scope.otherModelValue;
            };
            
            // listen to the change of password
            scope.$watch("otherModelValue", function() {
                ngModel.$validate();
            });
        }
    };
});
