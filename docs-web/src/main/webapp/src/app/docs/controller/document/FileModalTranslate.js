 'use strict';

/**
 * File translation modal controller.
 */
angular.module('docs').controller('FileModalTranslate', function($scope, $uibModalInstance, $interval, $translate, fileId, targetLanguage) {
  console.log('FileModalTranslate controller initialized');
  console.log('File ID:', fileId);
  
  // Initialize scope variables
  $scope.targetLanguage = targetLanguage;
  $scope.translating = false;
  $scope.progress = 0;
  
  // List of accepted languages
  $scope.acceptedLanguages = [
    { key: 'en', label: 'English' },
    { key: 'fr', label: 'Français' },
    { key: 'es', label: 'Español' },
    { key: 'de', label: 'Deutsch' },
    { key: 'it', label: 'Italiano' },
    { key: 'pt', label: 'Português' },
    { key: 'ru', label: 'Русский' },
    { key: 'zh', label: '中文' },
    { key: 'ja', label: '日本語' },
    { key: 'ko', label: '한국어' }
  ];

  /**
   * Start the translation process.
   */
  $scope.startTranslation = function() {
    console.log('Starting translation with language:', $scope.targetLanguage);
    
    if (!$scope.targetLanguage) {
      console.log('No target language selected');
      return false;
    }

    $scope.translating = true;
    $scope.progress = 0;

    // Simulate progress updates
    var progressInterval = setInterval(function() {
      $scope.progress += 10;
      if ($scope.progress >= 100) {
        clearInterval(progressInterval);
      }
      $scope.$apply();
    }, 500);

    // Close the modal with the selected language
    $uibModalInstance.close($scope.targetLanguage);
  };

  /**
   * Cancel the translation.
   */
  $scope.cancel = function() {
    console.log('Translation cancelled');
    $uibModalInstance.dismiss('cancel');
  };
});