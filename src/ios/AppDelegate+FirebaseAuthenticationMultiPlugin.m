#import "AppDelegate+FirebaseAuthenticationMultiPlugin.h"
#import <objc/runtime.h>
#import <GoogleSignIn/GoogleSignIn.h>

static void swizzleMethod(Class class, SEL destinationSelector, SEL sourceSelector);

@implementation AppDelegate (FirebaseAuthenticationMultiPlugin)

+ (void)load {

    swizzleMethod([AppDelegate class],
            @selector(application:openURL:options:),
            @selector(identity_application:openURL:options:));
    swizzleMethod([AppDelegate class],
            @selector(application:openURL:sourceApplication:annotation:),
            @selector(identity_application:openURL:sourceApplication:annotation:));
}

#pragma mark - AppDelegate Swizzles

- (BOOL)identity_application:(UIApplication *)app
                     openURL:(NSURL *)url
                     options:(NSDictionary<NSString *, id> *)options {
    return [[GIDSignIn sharedInstance] handleURL:url
                               sourceApplication:options[UIApplicationOpenURLOptionsSourceApplicationKey]
                                      annotation:options[UIApplicationOpenURLOptionsAnnotationKey]];
}

- (BOOL)identity_application:(UIApplication *)application
                     openURL:(NSURL *)url
           sourceApplication:(NSString *)sourceApplication
                  annotation:(id)annotation {
    return [[GIDSignIn sharedInstance] handleURL:url
                               sourceApplication:sourceApplication
                                      annotation:annotation];
}
@end

static void swizzleMethod(Class class, SEL destinationSelector, SEL sourceSelector) {
    Method destinationMethod = class_getInstanceMethod(class, destinationSelector);
    Method sourceMethod = class_getInstanceMethod(class, sourceSelector);

    // If the method doesn't exist, add it.  If it does exist, replace it with the given implementation.
    if (class_addMethod(class, destinationSelector, method_getImplementation(sourceMethod), method_getTypeEncoding(sourceMethod))) {
        class_replaceMethod(class, destinationSelector, method_getImplementation(destinationMethod), method_getTypeEncoding(destinationMethod));
    } else {
        method_exchangeImplementations(destinationMethod, sourceMethod);
    }
}
