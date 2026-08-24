import { useMutation, useQueryClient } from '@tanstack/react-query'
import { useNavigate } from 'react-router-dom'
import { cancelPostLike, likePost, type PostDetail } from '@/api/posts'
import { useAuth } from '@/auth/use-auth'
import { LikeButton } from '@/components/post/LikeButton'
import { paths } from '@/routes/paths'

export function PostLikeButton({ post }: { post: PostDetail }) {
  const { member } = useAuth()
  const navigate = useNavigate()
  const queryClient = useQueryClient()

  const mutation = useMutation({
    mutationFn: () => (post.likedByMe ? cancelPostLike(post.id) : likePost(post.id)),
    onSuccess: () => {
      // 추천수와 "내가 추천함"은 서버가 정하는 값이다. 낙관적으로 흉내내지 않고 다시 읽는다.
      queryClient.invalidateQueries({ queryKey: ['post', post.id] })
      queryClient.invalidateQueries({ queryKey: ['posts'] })
    },
  })

  return (
    <LikeButton
      count={post.likeCount}
      likedByMe={post.likedByMe}
      canLike={Boolean(member)}
      isPending={mutation.isPending}
      onToggle={() => (member ? mutation.mutate() : navigate(paths.login))}
    />
  )
}
