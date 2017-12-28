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
    
    NSDictionary *options = [command argumentAtIndex:0 withDefault:@{} andClass:[NSDictionary class]];

    @try {
        self.providers = [[NSMutableArray<id<FUIAuthProvider>> alloc] init];
    
        NSArray *providers = [options valueForKey:@"providers"];
        
        if (providers != nil) {
            
            BOOL emailHidden = true;
            
            for (NSString *provider in providers) {
                if ([provider isEqualToString:@"GOOGLE"]) {
                    [self.providers addObject:[[FUIGoogleAuth alloc] init]];
                }
                
                if ([provider isEqualToString:@"FACEBOOK"]) {
                    [self.providers addObject:[[FUIFacebookAuth alloc] init]];
                }
                
                if ([provider isEqualToString:@"EMAIL"]) {
                    emailHidden = false;
                }
            }
            
            self.authUI.providers = self.providers;
        
            [self.authUI setSignInWithEmailHidden:emailHidden];
        }
        
        NSString *tosUrl = [options valueForKey:@"tosUrl"];
        
        if (tosUrl != nil) {
            NSURL *url = [NSURL URLWithString:tosUrl];
            [self.authUI setTOSURL:url];
        }
    }
    @catch (NSException *exception) {
        NSLog(@"Initialise error %@", [exception reason]);
    }
}

- (void)getToken:(CDVInvokedUrlCommand *)command {
}

- (void)signIn:(CDVInvokedUrlCommand *)command {
    
    self.signInCallbackId = command.callbackId;
    
    @try {
        UINavigationController *authViewController = [self.authUI authViewController];
        [self.viewController presentViewController:authViewController animated:YES completion:nil];
    }
    @catch (NSException *exception) {
        NSLog(@"SignIn error %@", [exception reason]);
    }
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
    
    NSDictionary *result = @{@"email" : [user email],
                             @"name" : [user displayName],
                             @"token" : [user refreshToken],
                             @"id" : [user uid],
                             @"photoUrl" : [user photoURL]
                             };
    
    CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsDictionary:result];
    [pluginResult setKeepCallbackAsBool:YES];
    
    [self.commandDelegate sendPluginResult:pluginResult callbackId:self.signInCallbackId];
}

@end
