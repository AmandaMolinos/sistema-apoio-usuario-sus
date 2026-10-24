package br.com.apoiosus.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import br.com.apoiosus.service.SessaoService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private static final String PREFIXO = "Bearer ";

    private final SessaoService sessaoService;

    public TokenAuthenticationFilter(SessaoService sessaoService) {
        this.sessaoService = sessaoService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String cabecalho = request.getHeader("Authorization");
        if (cabecalho != null && cabecalho.startsWith(PREFIXO)) {
            sessaoService.buscarValida(cabecalho.substring(PREFIXO.length()).trim()).ifPresent(sessao -> {
                var principal = new UsuarioAutenticado(sessao.getUsuarioId(), sessao.getId());
                SecurityContextHolder.getContext()
                        .setAuthentication(new UsernamePasswordAuthenticationToken(principal, null, List.of()));
            });
        }
        chain.doFilter(request, response);
    }
}
