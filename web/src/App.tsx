import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import type { ReactNode } from 'react';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AppShell } from './components/organisms/AppShell';
import { AnalysisProvider } from './hooks/AnalysisContext';
import { ProfileProvider, useProfile } from './hooks/ProfileContext';
import { AnalyzeRoute } from './pages/AnalyzePage';
import { CommunityRoute } from './pages/CommunityPage';
import { HistoryRoute } from './pages/HistoryPage';
import { HomeRoute } from './pages/HomePage';
import { ProfilePage } from './pages/ProfilePage';
import { WelcomePage } from './pages/WelcomePage';

const queryClient = new QueryClient({
  defaultOptions: { queries: { retry: 1, refetchOnWindowFocus: true, staleTime: 10_000 } },
});

/** Onboarding only runs until a profile is saved in this browser. */
function RequireProfile({ children }: { children: ReactNode }) {
  const { profile } = useProfile();
  return profile ? <>{children}</> : <Navigate to="/welcome" replace />;
}

export function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <ProfileProvider>
        <AnalysisProvider>
          <BrowserRouter>
            <Routes>
              <Route path="/welcome" element={<WelcomePage />} />
              <Route path="/profile" element={<ProfilePage />} />
              <Route
                element={
                  <RequireProfile>
                    <AppShell />
                  </RequireProfile>
                }
              >
                <Route index element={<HomeRoute />} />
                <Route path="analyze" element={<AnalyzeRoute />} />
                <Route path="history" element={<HistoryRoute />} />
                <Route path="community" element={<CommunityRoute />} />
              </Route>
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </BrowserRouter>
        </AnalysisProvider>
      </ProfileProvider>
    </QueryClientProvider>
  );
}
