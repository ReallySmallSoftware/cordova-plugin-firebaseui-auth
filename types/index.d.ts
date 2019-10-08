

declare namespace FirebaseUIAuth {
    export interface FirebaseUIAuthUserDetail {
        name: string | null;
        email: string | null;
        emailVerified: boolean;
        id: string | null;
        newUser: boolean;
    }

    export interface FirebaseUIAuthUser {
        detail: FirebaseUIAuthUserDetail;
    }

    export type FirebaseUIAuthOptionsProviders = "EMAIL" | "FACEBOOK" | "GOOGLE" | "PHONE" | "TWITTER" | "GITHUB" | "ANONYMOUS";

    export interface FirebaseUIAuthOptions {
        anonymous?: boolean;
        providers?: FirebaseUIAuthOptionsProviders[];
        logo?: string;
        theme?: string;
        tosUrl?: string;
        privacyPolicyUrl?: string;
        smartLockEnabled?: boolean;
        smartLockHints?: boolean;
        uiElement?: string;
        browser?: any;
    }

    export function initialise(options: FirebaseUIAuthOptions): Promise<void>;

    export interface FirebaseUIAuth {

        getToken(): string;
        getCurrentUser(): Promise<FirebaseUIAuthUser>;
        signIn(): void;
        signInAnonymously(): void;
        signOut(): void;
        delete(): void;
        sendEmailVerification(): void;
        reloadUser(): void;
    }
}