#import "FlutterWindowmanagerPlusPlugin.h"
#if __has_include(<flutter_windowmanager_plus/flutter_windowmanager_plus-Swift.h>)
#import <flutter_windowmanager_plus/flutter_windowmanager_plus-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "flutter_windowmanager_plus-Swift.h"
#endif

@implementation FlutterWindowmanagerPlusPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftFlutterWindowmanagerPlusPlugin registerWithRegistrar:registrar];
}
@end
