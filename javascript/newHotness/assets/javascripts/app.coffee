define [
    'angular'
    'c/controllers'
    'd/directives'
    'filters/filters'
    'responseInterceptors/responseInterceptors'
    'services/services'
    ], (angular) ->
    'use strict'
    angular.module 'app', [
        'controllers'
        'directives'
        'filters'
        'responseInterceptors'
        'services'
    ]