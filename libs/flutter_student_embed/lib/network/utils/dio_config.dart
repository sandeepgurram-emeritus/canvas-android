// Copyright (C) 2020 - present Instructure, Inc.
//
// This program is free software: you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation, version 3 of the License.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <http://www.gnu.org/licenses/>.

import 'dart:io';

import 'package:dio/dio.dart';
import 'package:dio_http_cache/dio_http_cache.dart';
import 'package:flutter/widgets.dart';
import 'package:flutter_student_embed/network/utils/api_prefs.dart';
import 'package:flutter_student_embed/utils/debug_flags.dart';

/// Class that helps to create and configure [Dio] instances for common use cases
class DioConfig {
  final String baseUrl;
  final Map<String, String> baseHeaders;
  final Duration cacheMaxAge;
  final bool forceRefresh;
  final PageSize pageSize;
  final Map<String, dynamic> extraQueryParams;

  DioConfig({
    this.baseUrl = '',
    Map<String, String> baseHeaders,
    this.cacheMaxAge = Duration.zero,
    this.forceRefresh = false,
    this.pageSize = PageSize.none,
    this.extraQueryParams,
  })  : this.baseHeaders = baseHeaders ?? {},
        assert(baseUrl != null),
        assert(cacheMaxAge != null),
        assert(forceRefresh != null),
        assert(pageSize != null);

  /// Creates a copy of this configuration with the given fields replaced with the new values
  DioConfig copyWith({
    String baseUrl,
    Map<String, String> baseHeaders,
    Duration cacheMaxAge,
    bool forceRefresh,
    PageSize pageSize,
    Map<String, dynamic> extraQueryParams,
  }) {
    return DioConfig(
      baseUrl: baseUrl ?? this.baseUrl,
      baseHeaders: baseHeaders ?? this.baseHeaders,
      cacheMaxAge: cacheMaxAge ?? this.cacheMaxAge,
      forceRefresh: forceRefresh ?? this.forceRefresh,
      pageSize: pageSize ?? this.pageSize,
      extraQueryParams: extraQueryParams ?? this.extraQueryParams,
    );
  }

  /// Creates a [Dio] instance using this configuration
  Dio get dio {
    // Add canvas-string-ids header to ensure Canvas IDs are returned as Strings
    baseHeaders[HttpHeaders.acceptHeader] =
        'application/json+canvas-string-ids';

    // Configure base options
    var options = BaseOptions(baseUrl: baseUrl, headers: baseHeaders);

    // Add per_page query param if requested
    Map<String, dynamic> extraParams = extraQueryParams ?? {};
    if (pageSize.size > 0) extraParams['per_page'] = pageSize.size;

    // Set extra query params
    options.queryParameters = extraParams;

    // Add cache configuration to base options
    if (cacheMaxAge != Duration.zero) {
      var extras =
          buildCacheOptions(cacheMaxAge, forceRefresh: forceRefresh).extra;
      options.extra.addAll(extras);
    }

    // Create Dio instance and add interceptors
    final dio = Dio(options);

    // Cache manager
    if (cacheMaxAge != Duration.zero) {
      dio.interceptors.add(_cacheInterceptor());
    }

    bool debug = DebugFlags.isDebugApi;

    // Log interceptor
    dio.interceptors.add(LogInterceptor(
      request: debug,
      requestHeader: debug,
      requestBody: debug,
      responseHeader: debug,
      responseBody: debug,
      error: debug,
    ));

    return dio;
  }

  Interceptor _cacheInterceptor() {
    Interceptor interceptor =
        DioCacheManager(CacheConfig(baseUrl: baseUrl)).interceptor;
    return InterceptorsWrapper(
      onRequest: (RequestOptions options, RequestInterceptorHandler handler) =>
          options.method == 'GET'
              ? interceptor.onRequest(options, handler)
              : handler.next(options),
      onResponse: (Response response, ResponseInterceptorHandler handler) =>
          response.requestOptions.method == 'GET'
              ? interceptor.onResponse(response, handler)
              : handler.next(response),
      onError: (DioError e, ErrorInterceptorHandler handler) => handler.next(
          e), // interceptor falls back to cache on error, a behavior we currently don't want
    );
  }

  /// Creates a [DioConfig] targeted at typical Canvas API usage
  static DioConfig canvas({
    bool includeApiPath: true,
    bool forceRefresh: false,
    bool forceDeviceLanguage: false,
    String overrideToken,
    Map<String, String> extraHeaders,
    PageSize pageSize: PageSize.none,
  }) {
    String masqueradeId = ApiPrefs.getCurrentLogin()?.masqueradeId;
    Map<String, dynamic> extraParams =
        masqueradeId == null ? null : {'as_user_id': masqueradeId};
    return DioConfig(
      baseUrl:
          includeApiPath ? ApiPrefs.getApiUrl() : '${ApiPrefs.getDomain()}/',
      baseHeaders: ApiPrefs.getHeaderMap(
        forceDeviceLanguage: forceDeviceLanguage,
        token: overrideToken,
        extraHeaders: extraHeaders,
      ),
      cacheMaxAge: const Duration(hours: 1),
      forceRefresh: forceRefresh,
      pageSize: pageSize,
      extraQueryParams: extraParams,
    );
  }

  /// Creates a [DioConfig] targeted at core/free-for-teacher API usage (i.e. canvas.instructure.com)
  static DioConfig core({
    bool includeApiPath: true,
    Map<String, String> headers,
    Duration cacheMaxAge: Duration.zero,
    bool forceRefresh: false,
    PageSize pageSize: PageSize.none,
  }) {
    var baseUrl = ApiPrefs.baseUrl;
    if (includeApiPath) baseUrl += 'api/v1/';

    return DioConfig(
        baseUrl: baseUrl,
        baseHeaders: headers,
        cacheMaxAge: cacheMaxAge,
        forceRefresh: forceRefresh,
        pageSize: pageSize);
  }

  /// Clears the cache, deleting only the entries related to path OR clearing everything if path is null
  Future<bool> clearCache({String path}) {
    // The methods below are currently broken in unit tests due to sqflite (even when the sqflite MethodChannel has been
    // mocked) so we'll just return 'true' for tests. See https://github.com/tekartik/sqflite/issues/83.
    if (WidgetsBinding.instance.runtimeType != WidgetsFlutterBinding)
      return Future.value(true);

    if (path == null) {
      return DioCacheManager(CacheConfig(baseUrl: baseUrl)).clearAll();
    } else {
      return DioCacheManager(CacheConfig(baseUrl: baseUrl))
          .deleteByPrimaryKey(path, requestMethod: "GET");
    }
  }
}

/// Class for configuring paging parameters
class PageSize {
  final int size;

  const PageSize(this.size);

  static const PageSize none = const PageSize(0);

  static const PageSize canvasDefault = const PageSize(10);

  static const PageSize canvasMax = const PageSize(100);

  @override
  bool operator ==(Object other) =>
      identical(this, other) || other is PageSize && this.size == other.size;
}

/// Convenience method that returns a [Dio] instance configured by calling through to [DioConfig.canvas]
Dio canvasDio({
  bool includeApiPath: true,
  bool forceRefresh: false,
  bool forceDeviceLanguage: false,
  String overrideToken,
  Map<String, String> extraHeaders,
  PageSize pageSize: PageSize.none,
}) {
  return DioConfig.canvas(
    forceRefresh: forceRefresh,
    forceDeviceLanguage: forceDeviceLanguage,
    overrideToken: overrideToken,
    extraHeaders: extraHeaders,
    pageSize: pageSize,
    includeApiPath: includeApiPath,
  ).dio;
}
