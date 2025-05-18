'use strict';

/**
 * File view controller.
 */
angular.module('docs').controller('FileView', function($uibModal, $state, $stateParams, $timeout, $scope, $translate, Restangular, $dialog) {
  console.log('FileView controller initialized');
  
  var modal = $uibModal.open({
    windowClass: 'modal modal-fileview',
    templateUrl: 'partial/docs/file.view.html',
    controller: 'FileModalView',
    size: 'lg',
    resolve: {
      fileId: function() {
        return $stateParams.fileId;
      }
    }
  });

  /**
   * Translate the current file.
   */
  $scope.translateFile = function() {
    console.log('File translation button clicked');
    console.log('Current file ID:', $stateParams.fileId);
    
    // Open language selection modal
    var translateModal = $uibModal.open({
      templateUrl: 'partial/docs/file.translate.html',
      controller: 'FileModalTranslate',
      size: 'sm',
      resolve: {
        fileId: function() {
          return $stateParams.fileId;
        }
      }
    });

    // Handle language translation
    translateModal.result.then(function(targetLanguage) {
      console.log('Target language selected:', targetLanguage);
      
      if (!targetLanguage) {
        console.log('No target language selected, aborting translation');
        return;
      }

      // Start translation progress
      console.log('Opening progress modal');
      var progressModal = $uibModal.open({
        templateUrl: 'partial/docs/file.translate.html',
        controller: 'FileModalTranslate',
        backdrop: 'static',
        keyboard: false,
        size: 'sm',
        resolve: {
          fileId: function() {
            return $stateParams.fileId;
          },
          targetLanguage: function() {
            return targetLanguage;
          }
        }
      });

      // Call the translation API
      console.log('Calling translation API with language:', targetLanguage);
      Restangular.one('document', $stateParams.fileId).post('translate', {
        targetLanguage: targetLanguage
      }).then(function(data) {
        console.log('Translation API response:', data);
        
        // Close progress modal
        progressModal.close();
        
        // Refresh the file view
        $scope.loadFile();
      }).catch(function(error) {
        console.error('Translation API error:', error);
        
        // Close progress modal
        progressModal.close();
        
        // Show error message
        var title = $translate.instant('document.translate.error_title');
        var msg = $translate.instant('document.translate.error_message');
        var btns = [{result: 'ok', label: $translate.instant('ok'), cssClass: 'btn-primary'}];
        $dialog.messageBox(title, msg, btns);
      });
    }).catch(function(error) {
      console.error('Modal error:', error);
    });
  };

  // Returns to document view on file close
  modal.closed = false;
  modal.result.then(function() {
    modal.closed = true;
  }, function() {
    modal.closed = true;
    $timeout(function () {
      // After all router transitions are passed,
      // if we are still on the file route, go back to the document
      if ($state.current.name === 'document.view.content.file' || $state.current.name === 'document.default.file') {
        $state.go('^', {id: $stateParams.id});
      }
    });
  });
});