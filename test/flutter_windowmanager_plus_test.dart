import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_windowmanager_plus/flutter_windowmanager_plus.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();

  const channel = MethodChannel('flutter_windowmanager_plus');
  final log = <MethodCall>[];

  void mockChannel({Object? response = true}) {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (call) async {
      log.add(call);
      return response;
    });
  }

  setUp(() {
    log.clear();
    mockChannel();
  });

  tearDown(() {
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, null);
    debugDefaultTargetPlatformOverride = null;
  });

  group('on Android', () {
    setUp(() => debugDefaultTargetPlatformOverride = TargetPlatform.android);

    test('addFlags invokes the platform channel with the flags', () async {
      final result = await FlutterWindowManagerPlus.addFlags(
          FlutterWindowManagerPlus.FLAG_SECURE);

      expect(result, isTrue);
      expect(log, <Matcher>[
        isMethodCall('addFlags',
            arguments: {'flags': FlutterWindowManagerPlus.FLAG_SECURE}),
      ]);
    });

    test('clearFlags invokes the platform channel with the flags', () async {
      final result = await FlutterWindowManagerPlus.clearFlags(
          FlutterWindowManagerPlus.FLAG_SECURE);

      expect(result, isTrue);
      expect(log, <Matcher>[
        isMethodCall('clearFlags',
            arguments: {'flags': FlutterWindowManagerPlus.FLAG_SECURE}),
      ]);
    });

    test('setSecure invokes the platform channel', () async {
      await FlutterWindowManagerPlus.setSecure(true);
      await FlutterWindowManagerPlus.setSecure(false);

      expect(log, <Matcher>[
        isMethodCall('setSecure', arguments: {'setSecure': true}),
        isMethodCall('setSecure', arguments: {'setSecure': false}),
      ]);
    });

    test('ORed flags are forwarded as a single bitmask', () async {
      const flags = FlutterWindowManagerPlus.FLAG_SECURE |
          FlutterWindowManagerPlus.FLAG_KEEP_SCREEN_ON;

      await FlutterWindowManagerPlus.addFlags(flags);

      expect(log, <Matcher>[
        isMethodCall('addFlags', arguments: {'flags': flags}),
      ]);
    });

    test('a null platform response is reported as false, not a type error',
        () async {
      mockChannel(response: null);

      expect(
        await FlutterWindowManagerPlus.addFlags(
            FlutterWindowManagerPlus.FLAG_SECURE),
        isFalse,
      );
    });
  });

  group('on a non-Android platform', () {
    setUp(() => debugDefaultTargetPlatformOverride = TargetPlatform.iOS);

    test('addFlags returns false without touching the channel', () async {
      expect(
        await FlutterWindowManagerPlus.addFlags(
            FlutterWindowManagerPlus.FLAG_SECURE),
        isFalse,
      );
      expect(log, isEmpty);
    });

    test('clearFlags returns false without touching the channel', () async {
      expect(
        await FlutterWindowManagerPlus.clearFlags(
            FlutterWindowManagerPlus.FLAG_SECURE),
        isFalse,
      );
      expect(log, isEmpty);
    });

    test('setSecure returns false without touching the channel', () async {
      expect(await FlutterWindowManagerPlus.setSecure(true), isFalse);
      expect(log, isEmpty);
    });
  });
}
