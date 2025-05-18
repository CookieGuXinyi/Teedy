'use strict';

/**
 * File modal view controller.
 */
angular.module('docs').controller('FileModalView', function ($uibModalInstance, $scope, $state, $stateParams, $sce, Restangular, $transitions, $uibModal, $dialog, $translate) {
  console.log('FileModalView controller initialized');
  console.log('File ID:', $stateParams.fileId);
  
  $scope.fileId = $stateParams.fileId;

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

  $scope.sourceLanguage = '';
  $scope.targetLanguage = '';
  $scope.showTranslationOptions = false;
  $scope.translating = false;
  $scope.progress = 0;
  $scope.translatedContent = null;
    
  var setFile = function (files) {
    // Search current file
    _.each(files, function (value) {
      if (value.id === $stateParams.fileId) {
        $scope.file = value;
        $scope.trustedFileUrl = $sce.trustAsResourceUrl('../api/file/' + $stateParams.fileId + '/data');
      }
    });
  };

  // Load files
  Restangular.one('file/list').get({ id: $stateParams.id }).then(function (data) {
    $scope.files = data.files;
    setFile(data.files);

    // File not found, maybe it's a version
    if (!$scope.file) {
      Restangular.one('file/' + $stateParams.fileId + '/versions').get().then(function (data) {
        setFile(data.files);
      });
    }
  });

  /**
   * Return the next file.
   */
  $scope.nextFile = function () {
    var next = undefined;
    _.each($scope.files, function (value, key) {
      if (value.id === $stateParams.fileId) {
        next = $scope.files[key + 1];
      }
    });
    return next;
  };

  /**
   * Return the previous file.
   */
  $scope.previousFile = function () {
    var previous = undefined;
    _.each($scope.files, function (value, key) {
      if (value.id === $stateParams.fileId) {
        previous = $scope.files[key - 1];
      }
    });
    return previous;
  };

  /**
   * Navigate to the next file.
   */
  $scope.goNextFile = function () {
    var next = $scope.nextFile();
    if (next) {
      $state.go('^.file', { id: $stateParams.id, fileId: next.id });
    }
  };

  /**
   * Navigate to the previous file.
   */
  $scope.goPreviousFile = function () {
    var previous = $scope.previousFile();
    if (previous) {
      $state.go('^.file', { id: $stateParams.id, fileId: previous.id });
    }
  };

  /**
   * Open the file in a new window.
   */
  $scope.openFile = function () {
    window.open('../api/file/' + $stateParams.fileId + '/data');
  };

  /**
   * Open the file content a new window.
   */
  $scope.openFileContent = function () {
    window.open('../api/file/' + $stateParams.fileId + '/data?size=content');
  };

  /**
   * Print the file.
   */
  $scope.printFile = function () {
    var popup = window.open('../api/file/' + $stateParams.fileId + '/data', '_blank');
    popup.onload = function () {
      popup.print();
      popup.close();
    }
  };

  /**
   * Close the file preview.
   */
  $scope.closeFile = function () {
    $uibModalInstance.dismiss();
  };

  // Close the modal when the user exits this state
  var off = $transitions.onStart({}, function(transition) {
    if (!$uibModalInstance.closed) {
      if (transition.to().name === $state.current.name) {
        $uibModalInstance.close();
      } else {
        $uibModalInstance.dismiss();
      }
    }
    off();
  });

  /**
   * Return true if we can display the preview image.
   */
  $scope.canDisplayPreview = function () {
    return $scope.file && $scope.file.mimetype !== 'application/pdf';
  };

  /**
   * Select the source and target languages and translate the file.
   */
  $scope.translateNow = function () {
    if (!$scope.sourceLanguage || !$scope.targetLanguage) {
      console.warn('必须同时选择源语言和目标语言');
      return;
    }
  
    $scope.translating = true;
    console.log('开始翻译，源语言:', $scope.sourceLanguage, '目标语言:', $scope.targetLanguage);
  
    Restangular.one('file/translate').get({
      documentId: $stateParams.id,
      sourceLanguage: $scope.sourceLanguage,
      targetLanguage: $scope.targetLanguage,
      fileId: $stateParams.fileId
    }).then(function (data) {
      console.log('翻译成功:', data);
      $scope.translating = false;
      $scope.translatedContent = data.translatedText;
    }).catch(function (error) {
      console.error('翻译失败:', error);
      $scope.translating = false;
    });
  };
});