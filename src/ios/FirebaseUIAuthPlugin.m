#import "FirebaseUIAuthPlugin.h"
@import Firebase;
@import FirebaseAuthUI;
@import FirebaseGoogleAuthUI;
@import FirebaseFacebookAuthUI;

@implementation FirebaseUIAuthPlugin

- (void)pluginInitialize {
    if(![FIRApp defaultApp]) {
        [FIRApp configure];
    }
 
    self.authUI = [FUIAuth defaultAuthUI];
    self.authUI.delegate = self;
}

- (void)initialise:(CDVInvokedUrlCommand *)command {
    
    self.providers = [[NSMutableArray alloc] init];
    
    [self.providers addObject:[[FUIGoogleAuth alloc] init]];
    [self.providers addObject:[[FUIFacebookAuth alloc] init]];
}

- (void)getToken:(CDVInvokedUrlCommand *)command {
}

- (void)signIn:(CDVInvokedUrlCommand *)command {
    UINavigationController *authViewController = [self.authUI authViewController];
    [self.viewController presentViewController:authViewController animated:YES completion:nil];
}

- (BOOL)application:(UIApplication *)app
openURL:(NSURL *)url
options:(NSDictionary *)options {
    NSString *sourceApplication = options[UIApplicationOpenURLOptionsSourceApplicationKey];
    return [[FUIAuth defaultAuthUI] handleOpenURL:url sourceApplication:sourceApplication];
}

- (void)signOut:(CDVInvokedUrlCommand *)command {
   // [self.authUI signOut];
}

- (void)authUI:(nonnull FUIAuth *)authUI didSignInWithUser:(nullable FIRUser *)user error:(nullable NSError *)error {
    
}

@end

